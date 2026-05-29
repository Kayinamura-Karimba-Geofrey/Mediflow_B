package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.Patient;
import geofrey.project.Mediflow_B.repository.PatientRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Endpoints for managing patient registrations and details")
public class PatientController {
    @Autowired
    PatientRepository patientRepository;

    @Operation(summary = "Get all patients", description = "List all registered patients in the hospital. Supports pagination and sorting. Required Role: USER, DOCTOR, or ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public Page<Patient> getAllPatients(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    @Operation(summary = "Register new patient", description = "Create a new patient record. Required Role: ADMIN, RECEPTIONIST, USER, or PATIENT", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RECEPTIONIST') or hasRole('USER') or hasRole('PATIENT')")
    public Patient createPatient(@RequestBody Patient patient) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        boolean isPatientRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_PATIENT"));
        
        if (isPatientRole) {
            geofrey.project.Mediflow_B.security.services.UserDetailsImpl userDetails = (geofrey.project.Mediflow_B.security.services.UserDetailsImpl) auth.getPrincipal();
            geofrey.project.Mediflow_B.entity.User user = new geofrey.project.Mediflow_B.entity.User();
            user.setId(userDetails.getId());
            patient.setUser(user);
        }
        patient.setId(null);
        return patientRepository.save(patient);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient patientDetails) {
        return patientRepository.findById(id)
                .map(patient -> {
                    patient.setName(patientDetails.getName());
                    patient.setDisease(patientDetails.getDisease());
                    patient.setEmail(patientDetails.getEmail());
                    patient.setPhone(patientDetails.getPhone());
                    return ResponseEntity.ok(patientRepository.save(patient));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(patient -> {
                    patientRepository.delete(patient);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
