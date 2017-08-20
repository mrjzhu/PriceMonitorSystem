package com.cs504_2.capstone;

import java.sql.*;
import java.util.Arrays;


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
    private Boolean isRecordExist(String sql_string) throws SQLException {
        PreparedStatement existStatement = null;
        boolean isExist = false;

        try
        {
            existStatement = d_connect.prepareStatement(sql_string);
            ResultSet result_set = existStatement.executeQuery();
            if (result_set.next())
            {
                isExist = true;
            }
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (existStatement != null)
            {
                existStatement.close();
            };
        }

        return isExist;
    }
    public void addAdData(Product prod) throws Exception{
        boolean isExist = false;

        PreparedStatement prod_info = null;
        String sql_string = "select * from " + d_db_name + ".product  where detail_url='" + prod.detail_url +"'";

        try
        {
            isExist = isRecordExist(sql_string);
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }

        if(isExist) {
            return;
        }


        sql_string = "insert into " + d_db_name +".product values(?,?,?,?,?)";
        try {
            prod_info = d_connect.prepareStatement(sql_string);
            prod_info.setString(1, prod.detail_url);
            prod_info.setString(2, prod.title);
            prod_info.setDouble(3, prod.price);
            prod_info.setDouble(4, prod.old_price);
            prod_info.setString(5, prod.category);
            prod_info.executeUpdate();
        }
        catch(SQLException e )
        {
            System.out.println(e.getMessage());
            throw e;
        }
        finally
        {
            if (prod_info != null) {
                prod_info.close();
            };
        }
    }
	

	 
     public void updateData(String detail_url,Double newPrice, Double oldPrice) throws Exception {
    	 PreparedStatement updateStatement = null;
         String sql_string= "update " + d_db_name + ".product set price=" + newPrice + ", old_price=" + oldPrice +" where detail_url='" + detail_url +"'";
         System.out.println("sql: " + sql_string);

         try
         {
        	 updateStatement = d_connect.prepareStatement(sql_string);
        	 updateStatement.executeUpdate();
         }
         catch(SQLException e )
          {
              System.out.println(e.getMessage());
              throw e;
          } 
          finally
          {
        	   if(updateStatement != null) {
        		   updateStatement.close();
        	   }
         }
    	 
     }
	
}
