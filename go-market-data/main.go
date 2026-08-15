package main

import (
	"encoding/json"
	"log"
	"os"
	"time"

	"github.com/gorilla/websocket"
	"github.com/joho/godotenv"
)

// FinnhubMessage is the raw envelope Finnhub sends.
type FinnhubMessage struct {
	Type string      `json:"type"`
	Data []TradeData `json:"data"`
}

// TradeData is one individual trade within a "trade" message.
type TradeData struct {
	Symbol    string  `json:"s"`
	Price     float64 `json:"p"`
	Volume    float64 `json:"v"`
	Timestamp int64   `json:"t"` // milliseconds since epoch
}

// Tick is our internal schema (per docs/market-data-schema.md),
// populated only with fields Finnhub's trade feed actually provides.
type Tick struct {
	Symbol    string  `json:"symbol"`
	Price     float64 `json:"price"`
	Volume    float64 `json:"volume"`
	Timestamp int64   `json:"timestamp"`
}

// symbols we want to track
var symbols = []string{"AAPL", "MSFT", "GOOGL", "AMZN"}

func main() {
	if err := godotenv.Load(); err != nil {
		log.Fatal("Error loading .env file")
	}

	apiKey := os.Getenv("FINNHUB_API_KEY")
	if apiKey == "" {
		log.Fatal("FINNHUB_API_KEY not set")
	}

	url := "wss://ws.finnhub.io?token=" + apiKey

	// Outer loop: reconnect forever if the connection drops.
	for {
		if err := connectAndListen(url); err != nil {
			log.Printf("connection error: %v — reconnecting in 5s", err)
			time.Sleep(5 * time.Second)
			continue
		}
	}
}

// connectAndListen dials Finnhub, subscribes, and reads until an error.
// Returning an error (rather than fatal) lets main() reconnect.
func connectAndListen(url string) error {
	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		return err
	}
	defer conn.Close()

	log.Println("Connected to Finnhub WebSocket")

	// Subscribe to every symbol in our list.
	for _, sym := range symbols {
		subMsg := map[string]string{"type": "subscribe", "symbol": sym}
		if err := conn.WriteJSON(subMsg); err != nil {
			return err
		}
		log.Printf("subscribed to %s", sym)
	}

	// Read loop.
	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			return err // bubble up so main() reconnects
		}

		var msg FinnhubMessage
		if err := json.Unmarshal(message, &msg); err != nil {
			log.Println("unmarshal error:", err)
			continue
		}

		switch msg.Type {
		case "ping":
			continue // heartbeat, ignore
		case "trade":
			for _, t := range msg.Data {
				tick := Tick{
					Symbol:    t.Symbol,
					Price:     t.Price,
					Volume:    t.Volume,
					Timestamp: t.Timestamp,
				}
				log.Printf("tick: %+v\n", tick)
			}
		default:
			log.Printf("unhandled message type: %s\n", msg.Type)
		}
	}
}