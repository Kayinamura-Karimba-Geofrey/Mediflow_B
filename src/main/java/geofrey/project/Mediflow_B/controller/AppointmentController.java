package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.Appointment;
import geofrey.project.Mediflow_B.entity.Patient;
import geofrey.project.Mediflow_B.repository.AppointmentRepository;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.List;

import java.time.LocalDateTime;
import java.time.LocalTime;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
@Tag(name = "Appointments", description = "Endpoints for scheduling and managing medical appointments")
public class AppointmentController {
    @Autowired
    AppointmentRepository appointmentRepository;

    @Autowired
    PatientRepository patientRepository;

    @Operation(summary = "Get all appointments", description = "List all appointments in the system. Supports pagination and sorting. Required Role: DOCTOR, ADMIN, or RECEPTIONIST", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN') or hasRole('RECEPTIONIST')")
    public Page<Appointment> getAllAppointments(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public List<Appointment> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Operation(summary = "Get my appointments", description = "List appointments for the currently logged-in patient.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Appointment>> getMyAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return patientRepository.findByUserId(userDetails.getId())
                .map(patient -> ResponseEntity.ok(appointmentRepository.findByPatientId(patient.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Appointment> getAppointmentsByPatient(@PathVariable Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Operation(summary = "Get today's appointments", description = "List all appointments scheduled for the current day. Required Role: RECEPTIONIST, ADMIN, or DOCTOR", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/today")
    @PreAuthorize("hasRole('RECEPTIONIST') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<Appointment> getTodaysAppointments() {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);
        return appointmentRepository.findByAppointmentDateBetween(start, end);
    }

    @Operation(summary = "Request/Create appointment", description = "Patients can request an appointment (status will be PENDING). Admin/Receptionist can create confirmed ones.")
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('PATIENT') or hasRole('RECEPTIONIST') or hasRole('ADMIN')")
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_PATIENT"));
        
        if (isPatient) {
            appointment.setStatus("PENDING");
            // Ensure patient is set to the logged-in user's patient profile
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Patient patient = patientRepository.findByUserId(userDetails.getId())
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Error: You must complete your Patient Profile before booking an appointment."));
            appointment.setPatient(patient);
        } else if (appointment.getStatus() == null) {
            appointment.setStatus("CONFIRMED");
        }
        
        appointment.setId(null);
        return appointmentRepository.save(appointment);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Approve/Update appointment status", description = "Admin can approve (set to CONFIRMED) or cancel appointments. Required Role: ADMIN", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Appointment> updateAppointmentStatus(@PathVariable Long id, @RequestParam String status) {
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    appointment.setStatus(status);
                    return ResponseEntity.ok(appointmentRepository.save(appointment));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
