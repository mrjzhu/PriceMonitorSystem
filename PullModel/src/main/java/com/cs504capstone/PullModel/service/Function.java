package com.cs504capstone.PullModel.service;

import com.cs504capstone.PullModel.Domain.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by zhujian on 8/10/17.
 */
public class Function {
    static private String mysqlHost = "127.0.0.1:3306";
    static private String mysqlDb = "Monitor_System";
    static private String mysqlUser = "root";
    static private String mysqlPass = "root";
    static MySQLAccess mysql;

    public Function(String mysqlHost,String mysqlDb,String user,String pass)
    {
        try {
            mysql = new MySQLAccess(mysqlHost, mysqlDb, user, pass);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    public List<Product> getPriceReducedProd(String email, String category) throws Exception {


        return mysql.getProductWithCategoryAndEmail(email, category);
    }

    public List<String> getCategories() throws Exception {


        return mysql.getCategories();
    }

    public Map<String,List<String>> getUserInterests() throws Exception {


        return mysql.getUserInterests();
    }

}
