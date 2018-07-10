package com.diplomska.crypto;

//STEP 1. Import required packages
import java.sql.*;

public class cryptoDB {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/cryptoDB";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "";

    public static String getKeyAlias(String userId){

        Connection conn = null;
        Statement stmt = null;
        String keyAlias = null;
        try{
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql = "SELECT key_alias FROM user_key WHERE user_id = " + userId;
            System.out.println("SQL: " + sql);
            ResultSet rs = stmt.executeQuery(sql);

            int columnCount = rs.getMetaData().getColumnCount();
            System.out.println(columnCount);

            if(columnCount == 1){
                rs.next();
                keyAlias = rs.getString("key_alias");
                System.out.println("KeyAlias: " + keyAlias);
            } else {
                System.out.println("Oops - sth weird is happening");
            }

            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        return keyAlias;
    }

    public static void updateKeyAlias(String userId, String keyAlias){

        Connection conn = null;
        Statement stmt = null;

        try{
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            // Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql = "SELECT key_alias FROM user_key WHERE user_id = " + userId;
            System.out.println("SQL: " + sql);
            ResultSet rs = stmt.executeQuery(sql);

            int columnCount = rs.getMetaData().getColumnCount();
            System.out.println(columnCount);

            if(columnCount == 1){
                // Existing user
                sql = "UPDATE user_key SET key_alias = " + keyAlias + " WHERE user_id =" +  userId;
                rs = stmt.executeQuery(sql);
            } else {
                sql = "INSERT INTO user_key VALUES (" + userId + ", '" + keyAlias + "')";
                rs = stmt.executeQuery(sql);
            }

            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }
}