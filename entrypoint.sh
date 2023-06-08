#!/bin/sh

rm -r data/kafka/* data/zookeeper/*
rm -r logs/*

nohup kafka_2.13-3.4.0/bin/zookeeper-server-start.sh kafka_2.13-3.4.0/config/zookeeper.properties > logs/zookeeper.log &
nohup kafka_2.13-3.4.0/bin/kafka-server-start.sh kafka_2.13-3.4.0/config/server.properties > logs/server.log &
sleep 5
cd Apache-Kafka-Project
nohup mvn compile -DskipTests > ../logs/Compilation.log &
sleep 5
nohup mvn exec:java -Dexec.mainClass='App' > ../logs/App.log &
sleep 5
nohup mvn exec:java -Dexec.mainClass='kafka.CoffeeStockConsumer' > ../logs/CoffeeStockConsumer.log &
nohup mvn exec:java -Dexec.mainClass='kafka.CoffeeStockProducer' > ../logs/CoffeeStockProducer.log &
nohup mvn exec:java -Dexec.mainClass='kafka.InterfaceConsumer' > ../logs/InterfaceConsumer.log &
nohup mvn exec:java -Dexec.mainClass='kafka.MergeCoffeePriceConsumer' > ../logs/MergeCoffeePriceConsumer.log &

sleep infinity