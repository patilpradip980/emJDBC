package com.bridgelabz.employeePayroll;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

public class DBConnect {
    public static void main(String[] args) {
        String jdbcURL = "jdbc:mysql://localhost:3306/EMPLOYEE_SERVICE?useSSL=false";
        String username = "root";
        String password = "root";
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("cannot find driver in classpath", exception);
        }
        listDriver();
        try {
            connection = DriverManager.getConnection(jdbcURL, username, password);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public static void listDriver() {
        Enumeration<Driver> driverList = DriverManager.getDrivers();
        while (driverList.hasMoreElements()) {
            Driver driverClass = driverList.nextElement();
            System.out.println(" " + driverClass.getClass().getName());
        }
    }
}
