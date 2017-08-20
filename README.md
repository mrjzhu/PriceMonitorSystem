# PriceMonitorSystem

## Introduction

Implemented an Amazon Product Price Monitor System to send product discount message to users based on subscription.

## Servers
1. Product_Crawler_Server
2. Price_Monitor_Server
3. Recommendation_Server
4. PullModel_Server

## Tools
Mysql, Memcached, RabbitMQ.

# Data Size

1. products on Amazon – 480 million
2. products_crawled – 20% (96 million), based on seeds selection

3. <key, value> store

    a. Key = detail_url (100 bytes), 
    
    b. Value = {product_id (16bytes), last_price(4bytes)} (100 + 16 + 4)×96×106 = 10.72𝐺𝐵

4. MySQL
    
   product_title (50 bytes), 
   
   product_category (20 bytes), 
   
   product_url (100 bytes), 
   
   current_price (4 bytes), 
   
   last_price (4 bytes),
   
   (50 + 20 + 100 + 4 + 4)×96×106 = 15.91GB
   
## FLow Diagram
![image](https://github.com/mrjzhu/PriceMonitorSystem/raw/master/architecute.jpeg)

## Steps

1. Get each category feed file from Feeds.

2. Used distributed crawler or multiple crawlers to crawl product data from Amazon, then put them into related topic based RabbitMQ.

3. Monitor Server retrieve data from each topic based RabbitMQ;

    a) Check on NoSQL database if is already existed.

        i.If existed, check if the price changed.
            1. If changed, set current price on price column and previous price
            
            on old price column; Then, put this product into Price Reduced
            
            Queue.


        ii.If not existed, store <Key: detailed url, Value:<productId, last price>> to NoSQL and all product information to MySQL.

4. User put their message<Email, Subscriptions > in MySQL database.

5. Recommendation Server retrieve price-reduced product and subscription info, send Email to Users based on their subscription.(Push Model)

6. User also can request Discount Message directly from MySQL database. (Pull Model)


