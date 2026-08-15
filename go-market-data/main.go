package main

import (
	"encoding/json"
	"log"
	"os"

	"github.com/gorilla/websocket"
	"github.com/joho/godotenv"
)

type FinnhubMessage struct{
	Type string			`json:"type"`
	Data []TradeData	`json:"data"`
}

//TradeData is one individual trade within a "trade" message
type TradeData struct{
	Symbol string	`json:"s"`
	Price  float64	`json:"p"`
	Volume float64	`json:"v"`
	Timestamp int64	`json:"t"`	//milisecond since epoch
}

//Tick is our internal schema (per docs/market-data-schema.md),
// populated only with field FinnHub's Trade feed actually provides
type Tick struct{
	Symbol string	`json:"symbol"`
	Price  float64	`json:"price"`
	Volume float64	`json:"volume"`
	Timestamp int64	`json:"timestamp"`
}

func main() {
	// Load .env into environment variables
	if err := godotenv.Load(); err != nil {
		log.Fatal("Error loading .env file")
	}

	apiKey := os.Getenv("FINNHUB_API_KEY")
	if apiKey == "" {
		log.Fatal("FINNHUB_API_KEY not set")
	}

	url := "wss://ws.finnhub.io?token=" + apiKey

	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		log.Fatal("dial error:", err)
	}
	defer conn.Close()

	log.Println("Connected to Finnhub WebSocket")

	// Subscribe to a symbol
	subscribeMsg := `{"type":"subscribe","symbol":"AAPL"}`
	if err := conn.WriteMessage(websocket.TextMessage, []byte(subscribeMsg)); err != nil {
		log.Fatal("subscribe error:", err)
	}

	// Read incoming messages in a loop
	for {
		_, message, err := conn.ReadMessage()
		if err != nil {
			log.Println("read error:", err)
			return
		}
		
		var msg FinnhubMessage
		if err := json.Unmarshal(message, &msg); err != nil {
			log.Println("Unmarshal error:", err)
			continue
		}

		switch msg.Type{
		case "ping":
			//heartbeat - nothing to do , just skip it
			continue
		case "trade":
			for _, t := range msg.Data{
				tick := Tick{
					Symbol: t.Symbol,
					Price:	t.Price,
					Volume: t.Volume,
					Timestamp: t.Timestamp,
				}
				log.Printf("tick: %+v\n", tick)
			}
		default:
			log.Printf("Unhandled message type: %s\n", msg.Type)
		}
	}
}