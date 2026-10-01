package com.example.mcpserver.tools;

import com.example.mcpserver.model.Employee;
import com.example.mcpserver.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeTools {

    private static final Logger log = LoggerFactory.getLogger(EmployeeTools.class);
    private final EmployeeService employeeService;

    public EmployeeTools(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @McpTool(name = "get_employee", description = "Get employee details by their unique employee ID")
    public String getEmployee(
            @McpToolParam(description = "The unique employee ID", required = true) Long id) {
        log.info("MCP tool called: get_employee, employeeId={}", id);
        if (id == null) {
            return "Error: Employee ID cannot be null";
        }
        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);
            if (employee.isPresent()) {
                Employee emp = employee.get();
                return String.format("Employee Found - ID: %d, Name: %s, Email: %s, Department: %s, Designation: %s, Salary: %s, Active: %s",
                        emp.getId(), emp.getName(), emp.getEmail(), emp.getDepartment(), emp.getDesignation(),
                        emp.getSalary() != null ? emp.getSalary().toString() : "N/A", emp.getActive());
            } else {
                return "Employee with ID " + id + " not found";
            }
        } catch (Exception ex) {
            log.error("Database error while fetching employee with ID: {}", id, ex);
            return "Error: Failed to retrieve employee details due to database error.";
        }
    }

    @McpTool(name = "list_employees", description = "List all active employees in the organization")
    public List<Employee> listEmployees() {
        log.info("MCP tool called: list_employees");
        try {
            return employeeService.getAllActiveEmployees();
        } catch (Exception ex) {
            log.error("Database error while listing employees", ex);
            throw new RuntimeException("Failed to list employees due to database error.", ex);
        }
    }

    @McpTool(name = "search_employees", description = "Search employees by optional name, department, or designation. All parameters are optional.")
    public List<Employee> searchEmployees(
            @McpToolParam(description = "Optional employee name to filter by", required = false) String name,
            @McpToolParam(description = "Optional department to filter by (e.g. IT, HR, Sales)", required = false) String department,
            @McpToolParam(description = "Optional designation to filter by (e.g. Developer, Manager)", required = false) String designation) {
        log.info("MCP tool called: search_employees, name={}, department={}, designation={}", name, department, designation);
        try {
            return employeeService.searchEmployees(name, department, designation);
        } catch (Exception ex) {
            log.error("Database error while searching employees with name={}, department={}, designation={}", name, department, designation, ex);
            throw new RuntimeException("Failed to search employees due to database error.", ex);
        }
    }

    @McpTool(name = "count_employees", description = "Count active employees, optionally filtered by department")
    public String countEmployees(
            @McpToolParam(description = "Optional department name to count (e.g. IT). If omitted, counts all active employees.", required = false) String department) {
        log.info("MCP tool called: count_employees, department={}", department);
        try {
            long count = employeeService.countEmployees(department);
            if (department != null && !department.trim().isEmpty()) {
                return String.format("Total active employees in department '%s': %d", department.trim(), count);
            }
            return String.format("Total active employees: %d", count);
        } catch (Exception ex) {
            log.error("Database error while counting employees in department={}", department, ex);
            return "Error: Failed to count employees due to database error.";
        }
    }

    @McpTool(name = "employees_by_department", description = "Get all active employees belonging to a specific department")
    public List<Employee> employeesByDepartment(
            @McpToolParam(description = "Department name (e.g. IT, Engineering, HR)", required = true) String department) {
        log.info("MCP tool called: employees_by_department, department={}", department);
        try {
            return employeeService.getEmployeesByDepartment(department);
        } catch (Exception ex) {
            log.error("Database error while fetching employees by department: {}", department, ex);
            throw new RuntimeException("Failed to fetch employees by department due to database error.", ex);
        }
    }
}
