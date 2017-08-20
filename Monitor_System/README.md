# Monitor server

## Introduction
THis is a service to crawl Amazon products based on different categories.

## Dependency
1. Jsoup
2. Jackson
3. slf4j
4. spymemcached
5. mysql-connector
6. amqp-client

## File
1. ProductData.txt ( store result of each operation)
2. proxylist_bittiger.csv ( The proxy ip address, just in case of being blocked.)

## Architecture graph
![image](https://github.com/mrjzhu/PriceMonitorSystem/blob/master/Monitor_System/monitor_graph.png)
## Steps
1. crawl products data from Amazon website based on different categories.
2. check on memcached(key:detail_url, value: price)
3. if changed, update the price on key value store.
4. update Mysql db.
5. sent price-reduced product to price-reduced RabbitMQ queue. 
6. if not, continue.Repeat 2 - 4
7. store new product and update price changes.

## Note

For test purpose, set scheduler to retrieve data from topic based  queue once a minute.


