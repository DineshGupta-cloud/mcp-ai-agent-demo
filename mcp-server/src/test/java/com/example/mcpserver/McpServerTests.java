package com.example.mcpserver;

import com.example.mcpserver.model.Employee;
import com.example.mcpserver.repository.EmployeeRepository;
import com.example.mcpserver.service.EmployeeService;
import com.example.mcpserver.tools.CalculatorTools;
import com.example.mcpserver.tools.EmployeeTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class McpServerTests {

    @Autowired
    private CalculatorTools calculatorTools;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeTools employeeTools;

    private Employee emp1;
    private Employee emp2;
    private Employee emp3;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        emp1 = employeeRepository.save(new Employee(
                "Rahul Sharma",
                "rahul.sharma@example.com",
                "IT",
                "Developer",
                new BigDecimal("75000.00"),
                true
        ));

        emp2 = employeeRepository.save(new Employee(
                "Priya Patel",
                "priya.patel@example.com",
                "IT",
                "Architect",
                new BigDecimal("120000.00"),
                true
        ));

        emp3 = employeeRepository.save(new Employee(
                "Ankit Verma",
                "ankit.verma@example.com",
                "HR",
                "HR Manager",
                new BigDecimal("65000.00"),
                true
        ));

        // Inactive employee to test active filtering
        employeeRepository.save(new Employee(
                "Inactive John",
                "john@example.com",
                "IT",
                "Developer",
                new BigDecimal("50000.00"),
                false
        ));
    }

    // --- Calculator Tools Tests ---

    @Test
    void testCalculatorAdd() {
        double result = calculatorTools.addNumbers(25, 35);
        assertThat(result).isEqualTo(60.0);
    }

    @Test
    void testCalculatorSubtract() {
        double result = calculatorTools.subtractNumbers(50, 20);
        assertThat(result).isEqualTo(30.0);
    }

    @Test
    void testCalculatorMultiply() {
        double result = calculatorTools.multiplyNumbers(6, 7);
        assertThat(result).isEqualTo(42.0);
    }

    @Test
    void testCalculatorDivideValid() {
        String result = calculatorTools.divideNumbers(100, 4);
        assertThat(result).isEqualTo("25.0");
    }

    @Test
    void testCalculatorDivideByZero() {
        String result = calculatorTools.divideNumbers(10, 0);
        assertThat(result).contains("Error: Cannot divide by zero");
    }

    // --- EmployeeRepository Tests ---

    @Test
    void testRepositoryFindByActiveTrue() {
        List<Employee> actives = employeeRepository.findByActiveTrue();
        assertThat(actives).hasSize(3);
    }

    @Test
    void testRepositoryCountByDepartment() {
        long count = employeeRepository.countByDepartmentIgnoreCaseAndActiveTrue("IT");
        assertThat(count).isEqualTo(2);
    }

    // --- EmployeeService Tests ---

    @Test
    void testServiceGetEmployeeById() {
        Optional<Employee> found = employeeService.getEmployeeById(emp1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Rahul Sharma");
    }

    @Test
    void testServiceSearchEmployees() {
        List<Employee> itDevs = employeeService.searchEmployees("Rahul", "IT", "Developer");
        assertThat(itDevs).hasSize(1);
        assertThat(itDevs.get(0).getName()).isEqualTo("Rahul Sharma");
    }

    // --- MCP Employee Tools Tests ---

    @Test
    void testToolGetEmployee() {
        String response = employeeTools.getEmployee(emp1.getId());
        assertThat(response).contains("Rahul Sharma")
                .contains("IT")
                .contains("Developer")
                .contains("75000.00");

        String notFound = employeeTools.getEmployee(99999L);
        assertThat(notFound).contains("Employee with ID 99999 not found");
    }

    @Test
    void testToolListEmployees() {
        List<Employee> list = employeeTools.listEmployees();
        assertThat(list).hasSize(3);
        assertThat(list).extracting(Employee::getName)
                .contains("Rahul Sharma", "Priya Patel", "Ankit Verma");
    }

    @Test
    void testToolSearchByDepartment() {
        List<Employee> itEmployees = employeeTools.searchEmployees(null, "IT", null);
        assertThat(itEmployees).hasSize(2);
    }

    @Test
    void testToolSearchByName() {
        List<Employee> rahulList = employeeTools.searchEmployees("Rahul", null, null);
        assertThat(rahulList).hasSize(1);
        assertThat(rahulList.get(0).getName()).isEqualTo("Rahul Sharma");
    }

    @Test
    void testToolSearchByDesignation() {
        List<Employee> devs = employeeTools.searchEmployees(null, null, "Developer");
        assertThat(devs).hasSize(1);
        assertThat(devs.get(0).getName()).isEqualTo("Rahul Sharma");
    }

    @Test
    void testToolCountEmployeesAll() {
        String countMsg = employeeTools.countEmployees(null);
        assertThat(countMsg).contains("Total active employees: 3");
    }

    @Test
    void testToolCountEmployeesDepartment() {
        String countMsg = employeeTools.countEmployees("IT");
        assertThat(countMsg).contains("Total active employees in department 'IT': 2");
    }

    @Test
    void testToolEmployeesByDepartment() {
        List<Employee> hrEmployees = employeeTools.employeesByDepartment("HR");
        assertThat(hrEmployees).hasSize(1);
        assertThat(hrEmployees.get(0).getName()).isEqualTo("Ankit Verma");
    }
}
