package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.Doctor;
import geofrey.project.Mediflow_B.repository.DoctorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/doctors")
@Tag(name = "Doctors", description = "Endpoints for managing doctor profiles and staff")
public class DoctorController {
    @Autowired
    DoctorRepository doctorRepository;

    @Operation(summary = "Get all doctors", description = "List all registered doctor profiles")
    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Operation(summary = "Get doctor by ID", description = "Fetch detailed profile of a specific doctor")
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Find doctors by specialisation", description = "Filter doctor list by their medical specialty (e.g., Cardiology)")
    @GetMapping("/specialisation/{specialisation}")
    public List<Doctor> getDoctorsBySpecialisation(@PathVariable String specialisation) {
        return doctorRepository.findBySpecialisation(specialisation);
    }

    @Operation(summary = "Create new doctor", description = "Add a new doctor profile to the system. Required Role: ROLE_ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        doctor.setId(null);
        return doctorRepository.save(doctor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setName(doctorDetails.getName());
                    doctor.setSpecialisation(doctorDetails.getSpecialisation());
                    doctor.setEmail(doctorDetails.getEmail());
                    doctor.setPhone(doctorDetails.getPhone());
                    doctor.setDepartment(doctorDetails.getDepartment());
                    return ResponseEntity.ok(doctorRepository.save(doctor));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctorRepository.delete(doctor);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
