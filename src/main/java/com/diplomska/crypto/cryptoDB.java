package com.diplomska.crypto;

import java.sql.*;

public class cryptoDB {

    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/cryptoDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";


    public static String getKeyAlias(String patientId) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        String keyAlias = null;

        try{
            String selectStatement = "SELECT key_alias FROM user_key WHERE user_id = ?";

            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectStatement);
            preparedStatement.setString(1, patientId);

            ResultSet rs = preparedStatement.executeQuery();
            int columnCount = rs.getMetaData().getColumnCount();

            if(columnCount == 1){
                rs.next();
                try{
                    keyAlias = rs.getString("key_alias");
                } catch (Exception e){
                    e.printStackTrace();
                }

                System.out.println("KeyAlias: " + keyAlias);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }
        }

        return keyAlias;
    }

    public static boolean updateKeyAlias(String userId, String keyAlias) throws SQLException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        boolean err = false;

        try{
            String selectStatement = "SELECT COUNT(key_alias) AS userInDB FROM user_key WHERE user_id = ?";
            dbConnection = getDBConnection();
            preparedStatement = dbConnection.prepareStatement(selectStatement);
            preparedStatement.setString(1, userId);

            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            int count = rs.getInt("userInDB");
            System.out.println("Count: " + count);

            if(count == 1){
                String updateStatment = "UPDATE user_key SET key_alias = ? WHERE user_id = ?";
                preparedStatement = dbConnection.prepareStatement(updateStatment);
                preparedStatement.setString(1, keyAlias);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            } else {
                String insertStatement = "INSERT INTO user_key(user_id, key_alias, key_assigned) VALUES (?,?,?)";
                preparedStatement = dbConnection.prepareStatement(insertStatement);
                preparedStatement.setString(1, userId);
                preparedStatement.setString(2, keyAlias);
                preparedStatement.setTimestamp(3, getCurrentTimeStamp());
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e){
            e.printStackTrace();
            err = true;
        } finally {

            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (dbConnection != null) {
                dbConnection.close();
            }

        }

        return err;

    }

    private static Connection getDBConnection() {

        Connection dbConnection = null;

        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,DB_PASSWORD);
            return dbConnection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return dbConnection;
    }

    private static java.sql.Timestamp getCurrentTimeStamp() {

        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }

}