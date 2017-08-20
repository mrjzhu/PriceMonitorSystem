package com.cs504_2.capstone;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by jiayangan on 8/4/17.
 */
public class Runnable implements java.lang.Runnable {

    int times = 0;
    @Override
    public void run() {

        System.out.println("Round : " + ++times + " , Retrieve Product from reduced queue......");
        try {
            new Main().consumer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
