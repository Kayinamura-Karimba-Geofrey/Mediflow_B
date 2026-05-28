package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.Department;
import geofrey.project.Mediflow_B.repository.DepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/departments")
@Tag(name = "Departments", description = "Endpoints for managing hospital departments")
public class DepartmentController {

    @Autowired
    DepartmentRepository departmentRepository;

    @Operation(summary = "Get all departments", description = "List all hospital departments. Accessible by any authenticated user.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Operation(summary = "Get department by ID", description = "Fetch a specific department by its ID.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new department", description = "Add a new hospital department. Required Role: ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Department createDepartment(@RequestBody Department department) {
        return departmentRepository.save(department);
    }

    @Operation(summary = "Update department", description = "Update an existing department's details. Required Role: ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department departmentDetails) {
        return departmentRepository.findById(id)
                .map(department -> {
                    department.setName(departmentDetails.getName());
                    department.setDescription(departmentDetails.getDescription());
                    return ResponseEntity.ok(departmentRepository.save(department));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete department", description = "Remove a department from the system. Required Role: ADMIN",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        return departmentRepository.findById(id)
                .map(department -> {
                    departmentRepository.delete(department);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
