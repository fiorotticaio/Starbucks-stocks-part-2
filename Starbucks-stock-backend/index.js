require("dotenv")
const { Kafka } = require("kafkajs")
const cors = require("cors")
const express = require("express")


const app = express()
app.use(cors())

const kafka = new Kafka({
  clientId: "my-consumer",
  brokers: [`${process.env.KAFKA_HOST}:${process.env.KAFKA_PORT}`],
});

let COFFEE_PRICE = 0
let WEB_PRICE = 0
let API_PRICE = 0
const producer = kafka.producer();
const consumer_coffe_price = kafka.consumer({ groupId: "coffee-consumer-price" });
const consumer_web_price = kafka.consumer({ groupId: "coffee-consumer-web" });
const consumer_api_price = kafka.consumer({ groupId: "coffee-consumer-api" });

async function setup() {
  producer.connect();
  consumer_coffe_price.subscribe({ topic: "coffee_price" });
  consumer_web_price.subscribe({ topic: "web_coffee_price" });
  consumer_api_price.subscribe({ topic: "api_coffee_price" });

  await consumer_coffe_price.run({eachMessage: async ({ topic, message }) => {COFFEE_PRICE = message.value?.toString()}});
  await consumer_web_price.run({eachMessage: async ({ topic, message }) => {WEB_PRICE = message.value?.toString()}});
  await consumer_api_price.run({eachMessage: async ({ topic, message }) => {API_PRICE = message.value?.toString()}});
}

setup()

app.get('/buy/:id', async (req, res)=> {
  const {id} = req.params
  await producer.send({
    topic: "coffee_sales",
    messages: [{
      key: "price",
      value: `price,${id}` 
    }],
  });
  return res.status(200).json({})
})

app.get('/all', async (req, res)=> {
  res.status(200).send({
    coffee_price: COFFEE_PRICE,
    web_coffee_price: WEB_PRICE,
    api_coffee_price: API_PRICE,
  });
})

app.listen(3001, ()=>console.log("Backend listening on port 3001"))