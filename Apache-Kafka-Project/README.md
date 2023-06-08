# Apache-Kafka-Project

## Topics
```md
	1. coffee_stock
		1.1. Store the stock price from intraday request
		1.2. Has only one partition
		1.3. Replication factor of 1

	2. coffee_price 
		2.1. Store the coffee price merged from the real stock and web activites responses
		2.2. Has 3 partitions, one for each price
			2.2.1. Partition 0 = Stock coffee price
			2.2.2. Partition 1 = Web Page coffee price;
			2.2.3. Partition 2 = Merged coffee price
		2.3. Replication factor of 1
```
## Instructions

1. Clear all kafka cache before initializing producers and consumers (data/kafka and data/zookeeper)
2. Start Zookeeper
3. Start Kafka Server
4. Start App.java
5. Start CoffeeStockProducer (optional)
6. Start CoffeeStockConsumer 
7. Start MergeCoffeePriceConsumer


## Useful links
API documentation
https://www.alphavantage.co/documentation/

Endpoint for stabucks in each minute
https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&interval=1min&symbol=SBUX&apikey=suappikey

To have the API key just give the email on the main page https://www.alphavantage.co/support/#api-key
And them you substitute the suappikey with your key

## Ideias
-[x] usar partições diferentes

-[ ] verificar possivel vazamento quando não se usa close no consumer/producer

-[ ] usar ações do starbucks de diferentes regiões

-[ ] inferencia de eventos de cada partição sobre cada região

-[ ] gerar um evento caso starbucks america esteja maior que a europa

-[ ] usar criatividade e verificar riqueza da api


## Integration repository

- Use the [main repository](https://github.com/matheusschreiber/Starbucks-stocks) to run all application