## Características
- [x] O sistema deve herdar as características do domínio da etapa 1, preferencialmente;
- [x] Considerem uma arquitetura Web no desenvolvimento do projeto (back-end e front-end);
- [x] Considerem um cliente (front-end) que permita visualização dos dados de forma amigável.

## Funcionalidades
- [ ] implementem o “evento composto” da etapa 1 usando Kafka Streams;
  - [x] Mudar coffe stock consumer para kstream
  - [x] Mudar interface consumer para kstream
  - [x] Mudar merge coffee price para kstream
- [ ] Definam aplicações/topologias Kafka Streams que permitam a detecção de outras 3 situações de interesse no sistema;
- [ ] Usem operações Stateless e Statefull da DSL do Streams. Usem filtros, maps, joins, agregações, janelas temporais e/ou de eventos.
- [ ] Usem criatividade para incluir outras funcionalidades interessantes!

# Starbucks Stock Webapp

This is a full integrated Java + Javascript Kafka environment to test multiple events

## How to run

- Add the kafka_2.13-3.4.0 folder to the root of the project
- Use the start.sh script
- When you're done, use the stop.sh script

No parameters needed

### Docker

If you prefer a docker version (with the right maven and jdk) use:
```
docker compose up
```