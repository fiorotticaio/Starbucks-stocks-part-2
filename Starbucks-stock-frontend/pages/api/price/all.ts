import { Kafka } from "kafkajs";
import type { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

  try {
    const kafka = new Kafka({
      clientId: "my-consumer",
      brokers: ["localhost:9092"],
    });

    const consumer = kafka.consumer({ groupId: "coffee-interface-consumer" });

    consumer.connect();
    consumer.subscribe({ topic: "coffee_price" });

    let output: string | undefined = "";

    await consumer.run({
      eachMessage: async ({ topic, partition, message }) => {
        if (partition == 2) {
          output = message.value?.toString();
          consumer.disconnect()
          return res.status(200).json(output);
        }
      },
    });
    
  } catch (err) {
    return res
      .status(500)
      .json({ error: "Something went wrong while polling from kafka" });
  }
}