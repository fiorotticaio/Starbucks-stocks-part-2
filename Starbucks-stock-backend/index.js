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

let COFFEE_PRICE_0 = 0
let COFFEE_PRICE_1 = 0
let COFFEE_PRICE_2 = 0
const producer = kafka.producer();
const consumer = kafka.consumer({ groupId: "coffee-interface-consumer" });

async function setup() {
  producer.connect();
  consumer.connect();
  consumer.subscribe({ topic: "coffee_price" });

  await consumer.run({
    eachMessage: async ({ topic, message }) => {
      COFFEE_PRICE_0 = -1; // FIXME
      COFFEE_PRICE_1 = -1; // FIXME
      COFFEE_PRICE_2 = message.value?.toString();
    },
  });
}

setup()

app.get('/buy/:id', async (req, res)=> {
  const {id} = req.params
  await producer.send({
    topic: "coffee_sales",
    messages: [{ value: id }],
  });
  return res.status(200).json({})
})

app.get('/all', async (req, res)=> {
  res.status(200).send({
    coffee_price_0: COFFEE_PRICE_0,
    coffee_price_1: COFFEE_PRICE_1,
    coffee_price_2: COFFEE_PRICE_2,
  });
})

app.listen(3001, ()=>console.log("Backend listening on port 3001"))