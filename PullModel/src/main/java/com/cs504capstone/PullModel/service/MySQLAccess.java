package com.cs504capstone.PullModel.service;

import com.cs504capstone.PullModel.Domain.Product;

import java.sql.*;
import java.util.*;


public class MySQLAccess {
	private Connection d_connect =  null;
	private String d_user_name;
	private String d_password;
	private String d_server_name;
	private String d_db_name;
    public void close() throws Exception {
        System.out.println("Close database");
          try {
            if (d_connect != null) {
                d_connect.close();
            }
          } catch (Exception e) {
              throw e;
          }
   }
	public MySQLAccess(String server, String db, String user, String pass) throws Exception{
		d_user_name = user;
		d_password = pass;
		d_server_name = server;
		d_db_name = db;
		try {
     	   // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            //"jdbc:mysql://127.0.0.1:3306/searchads?user=root&password=bittiger2017"
            String conn = "jdbc:mysql://" + d_server_name + "/" + 
                    d_db_name+"?user="+d_user_name+"&password="+d_password;
            System.out.println("Connecting to database: " + conn);
            d_connect = DriverManager.getConnection(conn);
            System.out.println("Connected to database");
        } catch(Exception e) {
            throw e;
        }     		
	}

    public List<String> getCategories() throws Exception{
        PreparedStatement checkStatement = null;
        String sql =  "select name from " + d_db_name + ".category";


        try {
            checkStatement = d_connect.prepareStatement(sql);
            ResultSet res = checkStatement.executeQuery(sql);
            List<String> r = new ArrayList<>();
            while(res.next()){
                r.add(res.getString("name"));
            }




            return r;
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (checkStatement != null) {
                checkStatement.close();
            };
        }
    }

    public Map<String, List<String>> getUserInterests() throws Exception{
        PreparedStatement checkStatement = null;
        String sql =  "select *from " + d_db_name + ".subscription";

        Map<String, List<String>> map = new HashMap<>();
        try {
            checkStatement = d_connect.prepareStatement(sql);
            ResultSet res = checkStatement.executeQuery(sql);
            while(res.next()){
                String email = res.getString("userdEmail");
                int id = res.getInt("categoryId");
                String sql2 =  "select *from " + d_db_name + ".category where id="+id;
                PreparedStatement checkStatement2 = null;
                checkStatement2 = d_connect.prepareStatement(sql2);
                ResultSet res2 = checkStatement2.executeQuery(sql2);
                while(res2.next()) {
                    if (map.get(email) != null) {
                        map.get(email).add(res2.getString("name"));
                    } else {
                        map.put(email, new ArrayList<>());
                        map.get(email).add(res2.getString("name"));
                    }
                }
            }




            return map;
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (checkStatement != null) {
                checkStatement.close();
            };
        }
    }



    public List<Product> getProductWithCategoryAndEmail(String email, String category) throws Exception{

        PreparedStatement checkStatement = null;
        String sql = "select categoryId from " + d_db_name + ".subscription  where userdEmail='"+ email + "'";
        try {
            checkStatement = d_connect.prepareStatement(sql);
            ResultSet res = checkStatement.executeQuery(sql);
            Set<Integer> categoryId = new HashSet<>();
            while(res.next()){
                categoryId.add(res.getInt("categoryId"));
            }


            List<Product> Products = new ArrayList<>();


            for(Integer n: categoryId){
                sql =  "select name from " + d_db_name + ".category  where id=" + n;
                checkStatement = d_connect.prepareStatement(sql);
                res = checkStatement.executeQuery(sql);


                while(res.next()){

                    String sql2 = "select *from " + d_db_name + ".product  where price < old_price and category='" + res.getString("name")+"'";
                    checkStatement = d_connect.prepareStatement(sql2);
                    ResultSet res2 = checkStatement.executeQuery(sql2);
                    while(res2.next()) {
                        if(category != null && !res2.getString("category").equals(category)) continue;
                        Product product = new Product();
                        product.detail_url = res2.getString("detail_url");
                        product.category = res2.getString("category");
                        product.price = res2.getDouble("price");
                        product.old_price = res2.getDouble("old_price");
                        product.title = res2.getString("title");
                        Products.add(product);
                    }
                }
            }
            Collections.sort(Products, new Comparator<Product>() {
                @Override
                public int compare(Product product, Product t1) {
                    double a = (product.old_price - product.price) / product.old_price;
                    double b = (t1.old_price - t1.price) / t1.old_price;

                    return a < b ? 1 : -1;
                }
            });
            return Products;
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (checkStatement != null) {
                checkStatement.close();
            };
        }
    }



}
