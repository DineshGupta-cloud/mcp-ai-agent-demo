package com.example.mcpserver.service;

import com.example.mcpserver.model.Employee;
import com.example.mcpserver.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public Optional<Employee> getEmployeeById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return employeeRepository.findById(id);
    }

    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByActiveTrue();
    }

    public List<Employee> searchEmployees(String name, String department, String designation) {
        String n = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String d = (department != null && !department.trim().isEmpty()) ? department.trim() : null;
        String des = (designation != null && !designation.trim().isEmpty()) ? designation.trim() : null;
        return employeeRepository.searchEmployees(n, d, des);
    }

    public long countEmployees(String department) {
        if (department != null && !department.trim().isEmpty()) {
            return employeeRepository.countByDepartmentIgnoreCaseAndActiveTrue(department.trim());
        }
        return employeeRepository.countByActiveTrue();
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return getAllActiveEmployees();
        }
        return employeeRepository.findByDepartmentIgnoreCaseAndActiveTrue(department.trim());
    }

    @Transactional
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
}
