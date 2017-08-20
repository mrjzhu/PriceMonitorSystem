package com.cs504_2.capstone;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

public class Main {

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private static final Version LUCENE_VERSION = Version.LUCENE_40;
    private static final int totalPage = 3;
    private static final String EXCHANGE_NAME = "Product_Exchange";

    private static List<String> proxys;
    private static int index_Proxy = 0;

    private Logger logger = LoggerFactory.getLogger(Main.class);



    public void initProxyList() {
        proxys = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader("proxylist_bittiger.csv"))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxys.add(ip);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort", "61336");
    }

    public void changeProxy() {
        if (index_Proxy == proxys.size()) index_Proxy = 0;
        String proxy = proxys.get(index_Proxy++);
        System.setProperty("socksProxyHost", proxy);
        System.out.println(proxy);
    }

    public String getTitle(Document doc, int i){
        String titleSelector1 =" > div > div:nth-child(3) > div:nth-child(1) > a > h2";
        String select1 = "#result_" +Integer.toString(i) + titleSelector1;

        Element title = doc.select(select1).first();
        if(title != null){
            return title.text();
        }

        logger.error("FAIL TO GET TITLE! RETURN UNKNOWN");
        return "UNKNOWN";


    }

    public double getPrice(Document doc, int i) {
        String price_path = "#result_" + Integer.toString(i) + " > div > div:nth-child(4) > div:nth-child(1) > a > span";
        Element priceElement = doc.select(price_path).first();
        if(priceElement != null){
            String[] priceStr = priceElement.text().split("-");
            String[] prices = priceStr[0].trim().split(" ");
            String[] IntParts = prices.length > 1? prices[1].split(","):null;
            String IntPart = IntParts == null?"0":"";
            if(IntParts != null) {
                for (String str : IntParts) {
                    IntPart = IntPart + str.trim();
                }
            }
            String fraction = prices.length > 2? prices[2] :"00";
            double price = Double.parseDouble(IntPart+"."+fraction);

            return price;
        }
        return 0.00;
    }

    public String getProdUrl(Document doc, int i){
        String selector = "#result_"+ Integer.toString(i)+ " > div > div:nth-child(4) > div:nth-child(1) > a";
        Element element = doc.select(selector).first();
        if(element != null) {
            String url = element.attr("href");
            int index = url.indexOf("ref");
//            if(url.substring(0,1).equals("/")){
//                int index2 = url.indexOf("www");
//                url = url.substring(index2);
//            }
//            else{
                url = url.substring(0, index - 1);

//            }
//            System.out.println("url: "+ detail_url);
            return url;
        }
        logger.error("FAIL TO GET DETAIL URL! RETURN UNKNOWN");
        return "UNKNOWN";
    }

    public List<Product> getResultFromQuery(String url, String category) {
        List<Product> ProdList = new ArrayList<>();
        for (int page = 1; page <= totalPage; page++) {
            String url_page = url + "&page="+Integer.toString(page);
            logger.info("Crawler data from...." + url_page);
            try {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
                headers.put("Accept-Language", "en-US,en;q=0.8");

                Document doc = Jsoup.connect(url_page).userAgent(USER_AGENT).timeout(100000).get();
                if (doc == null) {
                    logger.error("Cannot connect url, ");
                    return null;
                }
                Elements prods = doc.select("li[data-asin]");

                int startId = Integer.parseInt(prods.first().id().split("_")[1]);//            System.out.println("product size: "+ prods.size());

                for (int i = startId; i < startId + prods.size(); i++) {
                    Product prod = new Product();
                    prod.category = category;
                    prod.detail_url = getProdUrl(doc, i);
                    prod.price = getPrice(doc,i);
                    prod.title = getTitle(doc,i);
                    i++;
                    if (prod.price <= 0) continue;
                    ProdList.add(prod);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return ProdList;
    }

    public List<String> Json(List<Product> ProdList){
        List<String> JsonStr = new ArrayList<>();
        int r = 0;
        for(Product pro: ProdList){
            r++;
            JSONObject obj = new JSONObject();
            obj.put("Title", pro.title);
            obj.put("category", pro.category);
            obj.put("detail_url", pro.detail_url);
            if(r % 10 == 1)
                obj.put("price", pro.price - 16);
            else
                obj.put("price", pro.price);
            JSONArray keyWords = new JSONArray();
            JsonStr.add(obj.toString());
        }
        return JsonStr;


    }

    public static void SentToQueue(String str) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

//        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
//        JSONObject obj = new JSONObject(str);
//
//        String routingKey = obj.get("category").toString();
        channel.queueDeclare("product_q", true, false, false, null);


        channel.basicPublish("", "product_q", null, str.getBytes());
    }
    public static void main(String[] args) throws IOException, TimeoutException {
	// write your code here

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Runnable testRunnable = new Runnable();

        executor.scheduleAtFixedRate(testRunnable,
                0,
                1,
                TimeUnit.MINUTES);


    }
}
