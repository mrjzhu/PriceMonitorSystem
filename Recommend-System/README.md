# Recommendation server

## Introduction
THis is a service to sent discount info to users based on their subscription.

## Dependency
1. Jsoup
2. Jackson
3. slf4j
6. amqp-client



## Steps
1. Retrieve product from price-reduced rabbitmq queue
2. For each category's product, sent email to users based subscription.


## Note

For test purpose, set scheduler to retrieve data from price reduced queue once a minute.

