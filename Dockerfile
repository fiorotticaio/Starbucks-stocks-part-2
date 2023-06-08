FROM debian:bullseye-slim

WORKDIR /app

COPY ./Apache-Kafka-Project ./Apache-Kafka-Project 
COPY ./kafka_2.13-3.4.0 ./kafka_2.13-3.4.0
COPY ./data ./data
COPY ./logs ./logs
COPY ./entrypoint.sh ./entrypoint.sh

RUN apt-get update 
RUN apt-get install -y openjdk-17-jdk 
RUN apt-get -y install maven

RUN chmod +x entrypoint.sh

CMD ["bash", "entrypoint.sh"]