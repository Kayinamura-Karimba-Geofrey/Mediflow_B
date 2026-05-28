package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.Prescription;
import geofrey.project.Mediflow_B.repository.PatientRepository;
import geofrey.project.Mediflow_B.repository.PrescriptionRepository;
import geofrey.project.Mediflow_B.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/prescriptions")
@Tag(name = "Prescriptions", description = "Medicine prescriptions and dosages")
public class PrescriptionController {
    @Autowired
    PrescriptionRepository prescriptionRepository;

    @Autowired
    PatientRepository patientRepository;

    @Operation(summary = "Get all prescriptions", description = "List all prescriptions. Required Role: DOCTOR or ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    @Operation(summary = "Get my prescriptions", description = "Patients can view their own medicine prescriptions.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Prescription>> getMyPrescriptions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return patientRepository.findByUserId(userDetails.getId())
                .map(patient -> ResponseEntity.ok(prescriptionRepository.findByPatientId(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public List<Prescription> getPrescriptionsByPatient(@PathVariable Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public Prescription createPrescription(@RequestBody Prescription prescription) {
        return prescriptionRepository.save(prescription);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long id) {
        return prescriptionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePrescription(@PathVariable Long id) {
        return prescriptionRepository.findById(id)
                .map(prescription -> {
                    prescriptionRepository.delete(prescription);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
