package com.bridgelabz.employeePayroll;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public class EmployeePayrollServiceTest {
    EmployeePayrollDBServices employeePayrollDBServices;

    @Before
    public void setUp() throws Exception {
        employeePayrollDBServices = new EmployeePayrollDBServices();
    }

    @Test
    public void givenEmployeePayrollInDB_whenRetrieved_ShouldMatchEmployeeCount() throws InvalidException {
        List<EmployeePayrollData> employeePayrollData = employeePayrollDBServices.readData();
        Assert.assertEquals(3, employeePayrollData.size());
    }

    @Test
    public void givenNewSalaryForEmployee_whenUpdated_ShouldMatchWithDBReturnValue() throws InvalidException {
        employeePayrollDBServices.updateData("Mark", 10000.00);
        boolean result = employeePayrollDBServices.checkEmployeeDataSyncWithDB("Mark");
        Assert.assertTrue(result);
    }

    @Test
    public void givenEmployeePayrollInDB_whenPassWrongTableName_ShouldThrowException() {
        try {
            List<EmployeePayrollData> employeePayrollData = employeePayrollDBServices.readData();
            Assert.assertEquals(3, employeePayrollData.size());
        } catch (InvalidException invalidException) {
            Assert.assertEquals(InvalidException.ExceptionType.SQL_EXCEPTION, invalidException.type);
        }
    }

    @Test
    public void givenNewSalaryForEmployee_whenProvideWrongSeqOfColumn_ShouldThrowException() {
        try {
            int result = employeePayrollDBServices.updateData("Terisa", 50000.00);
        } catch (InvalidException invalidException) {
            Assert.assertEquals(InvalidException.ExceptionType.SQL_UPDATE_ERROR, invalidException.type);
        }
    }

    @Test
    public void givenDateRange_whenRetrieved_ShouldSyncWithDB() throws InvalidException {
        LocalDate startDate = LocalDate.of(2018, 1, 1);
        LocalDate endDate = LocalDate.now();
        List<EmployeePayrollData> employeePayrollDataList = employeePayrollDBServices.readEmployeeDataForDateRange(startDate, endDate);
        Assert.assertEquals(3, employeePayrollDataList.size());
    }

    @Test
    public void givenPayrollData_whenAverageSalaryRetrievedByGender_shouldReturnProperValue() {
        Map<String, Double> averageSalaryByGender = employeePayrollDBServices.readAverageSalaryByGender();
        Assert.assertTrue(averageSalaryByGender.get("M").equals(20000.00) &&
                averageSalaryByGender.get("F").equals(60000.00));
    }

    @Test
    public void givenNewEmployee_whenAdded_shouldSyncWithDB() {
        employeePayrollDBServices.addEmployeeToPayroll("Jenni", 20000.00, LocalDate.now(), "M");
        boolean result = employeePayrollDBServices.checkEmployeeDataSyncWithDB("Mark");
        Assert.assertTrue(result);
    }

    @Test
    public void givenEmployee_whenDeleted_shouldSyncWithDB() {
        employeePayrollDBServices.deleteEmployeeData("Jenni");
        boolean result = employeePayrollDBServices.checkEmployeeDataSyncWithDB("Jenni");
        Assert.assertFalse(result);
    }

}

