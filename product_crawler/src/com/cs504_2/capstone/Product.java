package com.cs504_2.capstone;

import java.util.List;

/**
 * Created by zhujian on 8/8/17.
 */
public class Product {
    static int num = 0;
    public int product_id;
    public String title; // required
    public double price; // required
    public double old_price;
    public String detail_url; // required
    public String category;

    public Product() {
        product_id = ++num;
    }
}
