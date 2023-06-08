#!/bin/bash

# echo "Setting PATH variable"
# export PATH=/Documentos/disciplinas/5-semestre/kafka/Kafka-Project-Docker/Kafka_2XXXX/bin:$PATH

pbar() {
  BAR_WIDTH=$1
  PERCENT=0
  update_progress() {
    PERCENT=$(( $1 * 100 / $2 ))
    FILL=$(($PERCENT * $BAR_WIDTH / 100))
    BAR="["
    for i in $(seq 1 $BAR_WIDTH); do
      if [[ $i -le $FILL ]]; then
        BAR+="="
      else
        BAR+=" "
      fi
    done
    BAR+="]"
    printf "\r%s %d%%" "$BAR" "$PERCENT"
  }

  COLOR_RED="\033[31m"
  COLOR_GREEN="\033[32m"
  COLOR_YELLOW="\033[33m"
  COLOR_RESET="\033[0m"
  BAR="$(printf "%${BAR_WIDTH}s" "")"
  for i in $(seq 1 100); do
    update_progress "$i" "100"
    sleep 0.01
    if [[ $PERCENT -lt $((30)) ]]; then
      printf "${COLOR_RED}"
    elif [[ $PERCENT -lt $((80)) ]]; then
      printf "${COLOR_YELLOW}"
    else
      printf "${COLOR_GREEN}"
    fi
  done
  printf "${COLOR_RESET}\n"
}



echo "Cleaning Kafka Data" 
rm -r data/kafka/* data/zookeeper/*
rm -r logs/*

echo "Starting Zookeeper and Kafka Server"
pbar 20
nohup kafka_2.13-3.4.0/bin/zookeeper-server-start.sh kafka_2.13-3.4.0/config/zookeeper.properties > logs/zookeeper.log & 
nohup kafka_2.13-3.4.0/bin/kafka-server-start.sh kafka_2.13-3.4.0/config/server.properties > logs/server.log & 
sleep 1

echo "Compiling Maven Project"
pbar 20
cd Apache-Kafka-Project
nohup mvn compile -DskipTests > ../logs/Compilation.log &
sleep 1

echo "Setting topics..."
pbar 20
nohup mvn exec:java -Dexec.mainClass='App' > ../logs/App.log &
sleep 1

echo "Launching CoffeeStockConsumer"
pbar 20
nohup mvn exec:java -Dexec.mainClass='kafka.CoffeeStockConsumer' > ../logs/CoffeeStockConsumer.log &
sleep 1

echo "Launching CoffeeStockProducer"
pbar 20
nohup mvn exec:java -Dexec.mainClass='kafka.CoffeeStockProducer' > ../logs/CoffeeStockProducer.log &
sleep 1

echo "Launching InterfaceConsumer"
pbar 20
nohup mvn exec:java -Dexec.mainClass='kafka.InterfaceConsumer' > ../logs/InterfaceConsumer.log &
sleep 1

echo "Launching MergeCoffeePriceConsumer"
pbar 20
nohup mvn exec:java -Dexec.mainClass='kafka.MergeCoffeePriceConsumer' > ../logs/MergeCoffeePriceConsumer.log &
sleep 1

echo "Launching backend on web server"
pbar 20
cd ..
nohup node Starbucks-stock-backend/index.js > logs/Backend.log &
sleep 1

echo "Launching frontend on web server"
pbar 20
cd Starbucks-stock-frontend
nohup npm run dev > ../logs/Frontend.log &
sleep 1

echo "Final arrangements..."
pbar 20