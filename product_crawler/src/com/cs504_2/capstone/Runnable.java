package com.cs504_2.capstone;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.cs504_2.capstone.Main.SentToQueue;

/**
 * Created by jiayangan on 8/4/17.
 */
public class Runnable implements java.lang.Runnable {
    int times = 0;
    @Override
    public void run() {
        System.out.println("Round : " + ++times + " , put Product to product queue......");

        new Main().initProxyList();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("rawQuery.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File adsDataFile = new File("ProductData.txt");
        if (!adsDataFile.exists()) {
            try {
                adsDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileWriter fileWriterForAds = null;
        try {
            fileWriterForAds = new FileWriter(adsDataFile.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bufferedWriterForAds = new BufferedWriter(fileWriterForAds);

        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                new Main().changeProxy();
                if(line.length() == 0) continue;
                String[] mesg = line.split(",");

                List<Product> products = new Main().getResultFromQuery(mesg[0].trim(),mesg[1]);
                if (products == null) continue;


                List<String> JsonFile = new Main().Json(products);
                for(String str: JsonFile) {
                    SentToQueue(str);
                    try {
                        bufferedWriterForAds.write(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        bufferedWriterForAds.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        try {
            bufferedWriterForAds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
