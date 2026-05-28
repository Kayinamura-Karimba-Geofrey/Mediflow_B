package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.PatientRecord;
import geofrey.project.Mediflow_B.repository.PatientRecordRepository;
import geofrey.project.Mediflow_B.repository.PatientRepository;
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
@RequestMapping("/api/patient-records")
@Tag(name = "Patient Records", description = "Medical history and diagnosis records")
public class PatientRecordController {
    @Autowired
    PatientRecordRepository patientRecordRepository;

    @Autowired
    PatientRepository patientRepository;

    @Operation(summary = "Get all records", description = "List all medical records. Required Role: DOCTOR or ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public List<PatientRecord> getAllRecords() {
        return patientRecordRepository.findAll();
    }

    @Operation(summary = "Get my medical history", description = "Patients can view their own medical records.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PatientRecord>> getMyRecords() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return patientRepository.findByUserId(userDetails.getId())
                .map(patient -> ResponseEntity.ok(patientRecordRepository.findByPatientId(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public List<PatientRecord> getRecordsByPatient(@PathVariable Long patientId) {
        return patientRecordRepository.findByPatientId(patientId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public PatientRecord createRecord(@RequestBody PatientRecord record) {
        return patientRecordRepository.save(record);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PatientRecord> getRecordById(@PathVariable Long id) {
        return patientRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PatientRecord> updateRecord(@PathVariable Long id, @RequestBody PatientRecord recordDetails) {
        return patientRecordRepository.findById(id)
                .map(record -> {
                    record.setDiagnosis(recordDetails.getDiagnosis());
                    record.setTreatmentPlan(recordDetails.getTreatmentPlan());
                    return ResponseEntity.ok(patientRecordRepository.save(record));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
