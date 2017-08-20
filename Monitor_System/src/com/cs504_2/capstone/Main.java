package com.cs504_2.capstone;

import com.rabbitmq.client.*;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
    static private int EXP = 0;
    static private String memcachedServer = "127.0.0.1";
    static private String mysqlHost = "127.0.0.1:3306";
    static private String mysqlDb = "Monitor_System";
    static private String mysqlUser = "root";
    static private String mysqlPass = "root";
    static private int memcachedPortal = 11211;

    static private MySQLAccess mysql;
    static private MemcachedClient cache;

    public void consumer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.queueDeclare("product_q",true,false,false,null);

        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                try {
                    memcached(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        inChannel.basicConsume("product_q", true, consumer);
    }

    public static void InitDatabase(String memcachedServer,int memcachedPortal,String mysqlHost,String mysqlDb,String user,String pass)
    {
        try {
            mysql = new MySQLAccess(mysqlHost, mysqlDb, user, pass);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String address = memcachedServer + ":" + memcachedPortal;
        try
        {
            cache = new MemcachedClient(new ConnectionFactoryBuilder().setDaemon(true).setFailureMode(FailureMode.Retry).build(), AddrUtil.getAddresses(address));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void memcached(String str) throws Exception {
        JSONObject obj = new JSONObject(str);


        Product prod = new Product();
        prod.price = obj.getDouble("price");
        prod.detail_url = obj.getString("detail_url");
        prod.title = obj.getString("Title");
        prod.category = obj.getString("category");

        String key = prod.detail_url;
        if(cache.get(key) != null)
        {
            @SuppressWarnings("unchecked")
            Double oldPrice = (Double) cache.get(key);
            if(oldPrice == prod.price){
                return;
            }
            else{
                if(prod.price < oldPrice){
                    addToReducedQueue(str  + String.valueOf(oldPrice));
                }
                prod.old_price = oldPrice;
                cache.set(key, EXP, prod.price);
                mysql.updateData(prod.detail_url,prod.price,prod.old_price);
            }
        }
        else
        {
            cache.set(key, EXP, prod.price);
            mysql.addAdData(prod);
        }
    }


    public void addToReducedQueue(String str) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

//        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
//        JSONObject obj = new JSONObject(str);
//
//        String routingKey = obj.get("category").toString();
        channel.queueDeclare("reduced_q", true, false, false, null);


        channel.basicPublish("", "reduced_q", null, str.getBytes());
    }

    public static void main(String[] args) throws Exception {
	// write your code here

        InitDatabase(memcachedServer,memcachedPortal,mysqlHost,mysqlDb,mysqlUser,mysqlPass);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable testRunnable = new Runnable();

        executor.scheduleAtFixedRate(testRunnable,
                0,
                1,
                TimeUnit.MINUTES);
    }
}
