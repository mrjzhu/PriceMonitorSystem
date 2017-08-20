package com.cs504_2.capstone;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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
    public List<String> getUserEmail(String category) throws Exception{
        PreparedStatement checkStatement = null;
        String sql = "select id from " + d_db_name + ".category  where name='" + category + "'";


        try {
            checkStatement = d_connect.prepareStatement(sql);
            ResultSet res = checkStatement.executeQuery(sql);
            int id = 0;
            while(res.next()){
                id = res.getInt("id");
            }
            sql =  "select userdEmail from " + d_db_name + ".subscription  where categoryId=" + id;
            checkStatement = d_connect.prepareStatement(sql);
            res = checkStatement.executeQuery(sql);

            List<String> emails = new ArrayList<>();
            while(res.next()){
                emails.add(res.getString("userdEmail"));
            }
            return emails;
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
