package com.bridgelabz.employeePayroll;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollDBServices {

    public Connection getConnection() throws SQLException {
        String jdbcURL = "jdbc:mysql://localhost:3306/EMPLOYEE_SERVICE?useSSL=false";
        String username = "root";
        String password = "root";
        Connection connection;
        connection = DriverManager.getConnection(jdbcURL, username, password);
        return connection;
    }

    public List<EmployeePayrollData> getEmployeeDataUsingDB(String sql) throws InvalidException {
        List<EmployeePayrollData> employeePayrollDataList = new ArrayList<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            employeePayrollDataList = this.getEmployeePayrollData(resultSet);
        } catch (Exception exception) {
            throw new InvalidException(exception.getMessage(),
                    InvalidException.ExceptionType.SQL_EXCEPTION);
        }
        return employeePayrollDataList;
    }

    private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) throws SQLException {
        List<EmployeePayrollData> employeePayrollDataList = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            double salary = resultSet.getDouble("salary");
            LocalDate startDate = resultSet.getDate("start").toLocalDate();
            employeePayrollDataList.add(new EmployeePayrollData(id, name, salary, startDate));
        }
        return employeePayrollDataList;
    }

    public List<EmployeePayrollData> readData() throws InvalidException {
        String sql = " select * from EMPLOYEE; ";
        return this.getEmployeeDataUsingDB(sql);
    }

    public List<EmployeePayrollData> readEmployeeDataForDateRange(LocalDate startDate, LocalDate endDate) throws InvalidException {
        String sql = String.format("SELECT * FROM EMPLOYEE WHERE START BETWEEN '%S' AND '%S';",
                Date.valueOf(startDate), Date.valueOf(endDate));
        return this.getEmployeeDataUsingDB(sql);
    }


    public int updateData(String name, double salary) throws InvalidException {
        return this.updateDataUsingStatement(name, salary);
    }

    public int updateDataUsingStatement(String name, double salary) throws InvalidException {
        double deduction = salary * 0.2;
        double taxablePay = salary - deduction;
        double tax = taxablePay * 0.1;
        double netPay = salary - tax;
        int id = -1;
        int value = 0;
        String sql = String.format("update employee set salary=%.2f where name='%s';", salary, name);
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            value = statement.executeUpdate(sql);
            if (value == 1) {
                String s = "SELECT * FROM EMPLOYEE WHERE SALARY =" + salary;
                ResultSet rs = statement.executeQuery(s);
                while (rs.next()) {
                    id = rs.getInt("ID");
                }
                System.out.println(id);
                String sql3 = String.format("update payroll_details set basic_pay=%s," +
                                " deduction=%s,taxable_pay=%s,tax=%s,net_pay=%s where employee_id =%s;"
                        , salary, deduction, taxablePay, tax, netPay, id);
                value = statement.executeUpdate(sql3);
                return value;
            }
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return value;
    }

    public Map<String, Double> readAverageSalaryByGender() {
        String sql = "select gender,avg(salary) as avg_salary from employee group by gender;";
        Map<String, Double> genderAverageSalaryMap = new HashMap<>();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String gender = resultSet.getString("gender");
                double salary = resultSet.getDouble("avg_salary");
                genderAverageSalaryMap.put(gender, salary);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return genderAverageSalaryMap;
    }

    public EmployeePayrollData addEmployeeToPayrollUC7(String name, double salary, LocalDate date, String gender) {
        int employee_id = -1;
        EmployeePayrollData employeePayrollData = null;
        String sql = String.format("Insert into employee(name,gender,salary,start)" +
                "values('%s','%s',%s,'%s')", name, gender, salary, Date.valueOf(date));
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();
            int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
            if (rowAffected == 1) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) employee_id = resultSet.getInt(1);
            }
            employeePayrollData = new EmployeePayrollData(employee_id, name, salary, date);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return employeePayrollData;
    }

    public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate date, String gender) {
        int employee_id = -1;
        Connection connection = null;
        EmployeePayrollData employeePayrollData = null;
        try {
            connection = this.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException exception) {
            exception.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try (Statement statement = connection.createStatement();) {
            String sql = String.format("Insert into employee(name,gender,salary,start)" +
                    "values('%s','%s',%s,'%s')", name, gender, salary, Date.valueOf(date));
            int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
            if (rowAffected == 1) {
                ResultSet resultSet = statement.getGeneratedKeys();
                if (resultSet.next()) employee_id = resultSet.getInt(1);
            }
            employeePayrollData = new EmployeePayrollData(employee_id, name, salary, date);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        try (Statement statement = connection.createStatement()) {
            double deduction = salary * 0.2;
            double taxablePay = salary - deduction;
            double tax = taxablePay * 0.1;
            double netPay = salary - tax;
            String sql = String.format("Insert into payroll_details " +
                    "(employee_id,basic_pay,deduction,taxable_pay,tax,net_pay)values" +
                    "( %s,%s,%s,%s,%s,%s)", employee_id, salary, deduction, taxablePay, tax, netPay);
            int rowAffected = statement.executeUpdate(sql);
            if (rowAffected == 1) {
                employeePayrollData = new EmployeePayrollData(employee_id, name, salary, date);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            connection.commit();
        } catch (SQLException exception) {
            exception.printStackTrace();

        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return employeePayrollData;
    }

    public boolean checkEmployeeDataSyncWithDB(String name) {
        String sql = "select * from employee where name= ?";
        String fetchedName = null;
        try (Connection connection = this.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fetchedName = rs.getString("NAME");
                if (fetchedName.equalsIgnoreCase(name))
                    return true;
                else
                    return false;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public void deleteEmployeeData(String name) {
        String sql = "DELETE FROM EMPLOYEE WHERE NAME=?";
        try (Connection connection = this.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, name);
            int row = preparedStatement.executeUpdate();
            System.out.println("deleted row : " + row);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
