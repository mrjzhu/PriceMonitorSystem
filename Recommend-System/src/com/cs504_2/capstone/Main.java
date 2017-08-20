package com.cs504_2.capstone;

import com.rabbitmq.client.*;

import org.json.JSONObject;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {

    static private String mysqlHost = "127.0.0.1:3306";
    static private String mysqlDb = "Monitor_System";
    static private String mysqlUser = "root";
    static private String mysqlPass = "root";
    static MySQLAccess mysql;

    public void consumer() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.queueDeclare("reduced_q",true,false,false,null);

        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                try {
                    String[] str = message.split("}");
                    Double oldprice = Double.valueOf(str[1]);
                    String msg = str[0] + "}";
                    JSONObject obj = new JSONObject(msg);

                    Product prod = new Product();
                    prod.price = obj.getDouble("price");
                    prod.detail_url = obj.getString("detail_url");
                    prod.title = obj.getString("Title");
                    prod.category = obj.getString("category");
                    prod.old_price = oldprice;
                    List<String> emails = mysql.getUserEmail(prod.category);
                    Email(emails, prod);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        inChannel.basicConsume("reduced_q", true, consumer);
    }

    public static void InitDatabase(String mysqlHost,String mysqlDb,String user,String pass)
    {
        try {
            mysql = new MySQLAccess(mysqlHost, mysqlDb, user, pass);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    public void Email(List<String> recipients, Product prod) throws AddressException {
//        / Recipient's email ID needs to be mentioned.
        String to = "mrjzhu@gmail.com";//change accordingl


        InternetAddress[] addressTo = new InternetAddress[recipients.size()];
        for(int i = 0; i < addressTo.length; i++) {
            addressTo[i] = new InternetAddress(recipients.get(i));
        }

        // Sender's email ID needs to be mentioned
        String from = "mrjzhu@gmail.com";//change accordingly
        final String username = "mrjzhu@gmail.com";//change accordingly
        final String password = "Zjj2015!";//change accordingly

        // Assuming you are sending email through relay.jangosmtp.net
        String host = "smtp.gmail.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    addressTo);
            // Set Subject: header field
            Double dis = 100 * (prod.old_price - prod.price)/prod.old_price;

            String discount =String.format("%.0f%%",dis);
            message.setSubject(discount + "Discount For " + prod.category);
            // Now set the actual message
            message.setText("Title: "+prod.title+"\n\nLink:"+prod.detail_url+"\n\nPrice: $"+prod.price +"\n\noldPrice: $"+prod.old_price +"\n\nClick link to see details...");
            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws Exception {
	// write your code here

        InitDatabase(mysqlHost,mysqlDb,mysqlUser,mysqlPass);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable testRunnable = new Runnable();

        executor.scheduleAtFixedRate(testRunnable,
                0,
                1,
                TimeUnit.MINUTES);

//        Product prod = new Product();
//        prod.price = 99.99;
//        prod.detail_url = "https://www.amazon.com/Columbia-Silver-Sleeve-Window-XX-Large/dp/B01GF1C4Q0";
//        prod.title = "Nike Running Shoes";
//        prod.category = "Bike";
//        List<String> emails = mysql.getUserEmail(prod.category);
//        m.Email(emails, prod);
    }
}
