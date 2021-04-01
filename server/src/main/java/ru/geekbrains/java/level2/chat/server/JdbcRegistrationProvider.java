package ru.geekbrains.java.level2.chat.server;

import java.sql.*;

public class JdbcRegistrationProvider {
    private Connection connection;
    private Statement stmt;
    private PreparedStatement psInsert;

    public JdbcRegistrationProvider() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:mdb0.db");
            stmt = connection.createStatement();
//            stmt.executeUpdate("drop table if exists users;");
            stmt.executeUpdate("CREATE TABLE if not exists users (\n" +
                    "    id    INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                    "    name  TEXT,\n" +
                    "    login TEXT,\n" +
                    "    password TEXT\n" +
                    ");");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Невозможно подключиться к БД");
        }
//        disconnect();
    }

    public void disconnect() {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (psInsert != null) {
                psInsert.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
    public boolean userRegistration(String name, String login, String password){
        try {
            if (checkUser(login)) {
                return false;
            }
            stmt.executeUpdate(String.format("insert into users (name, login, password) values ('%s', '%s', '%s');",
                    name,
                    login,
                    password));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }
    private boolean checkUser(String login){
        try (ResultSet rs = stmt.executeQuery("select login from users where login = '" +login + "';")) {
            if (rs.next()){
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean checkLoginAndPassword (String login, String password) {
        System.out.println(login +" " + password);
        try (ResultSet rs = stmt.executeQuery("select * from users;")) {
            while (rs.next()) {
                if (rs.getString("login").equals(login)
                        && rs.getString("password").equals(password)){
                    return true;
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
