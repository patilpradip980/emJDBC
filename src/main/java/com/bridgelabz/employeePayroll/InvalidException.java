package com.bridgelabz.employeePayroll;

public class InvalidException extends Exception {

    enum ExceptionType {
        SQL_UPDATE_ERROR,SQL_QUERY_ERROR, SQL_EXCEPTION
    }

    ExceptionType type;

    public InvalidException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }
}
