package com.example.mcpserver.repository;

import com.example.mcpserver.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByActiveTrue();

    List<Employee> findByDepartmentIgnoreCaseAndActiveTrue(String department);

    long countByActiveTrue();

    long countByDepartmentIgnoreCaseAndActiveTrue(String department);

    @Query("SELECT e FROM Employee e WHERE e.active = true " +
            "AND (:name IS NULL OR :name = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:department IS NULL OR :department = '' OR LOWER(e.department) LIKE LOWER(CONCAT('%', :department, '%'))) " +
            "AND (:designation IS NULL OR :designation = '' OR LOWER(e.designation) LIKE LOWER(CONCAT('%', :designation, '%')))")
    List<Employee> searchEmployees(
            @Param("name") String name,
            @Param("department") String department,
            @Param("designation") String designation);
}
