import { Kafka } from 'kafkajs';
import type { NextApiRequest, NextApiResponse } from 'next'

export default function handler(req: NextApiRequest, res: NextApiResponse) {
  
  const value = req.query.value

  try {
    sendTokafka(value as string);   
  } catch (err) {
    return res.status(500).json({error: 'Something went wrong while sending to kafka' })
  } 
  
  return res.status(200).json({ name: 'John Doe' })
}

async function sendTokafka(value: string) {
  const kafka = new Kafka({
    clientId: "my-producer",
    brokers: ["localhost:9092"],
  });

  const producer = kafka.producer();

  await producer.connect();

  await producer.send({
    topic: "coffee_sales",
    messages: [{ value: value }],
  });

  producer.disconnect();
}
