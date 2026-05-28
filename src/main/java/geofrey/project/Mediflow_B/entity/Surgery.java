package geofrey.project.Mediflow_B.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "surgeries")
public class Surgery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String surgeryName;

    private LocalDateTime surgeryDate;
    private String outcome;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    public Surgery() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSurgeryName() { return surgeryName; }
    public void setSurgeryName(String surgeryName) { this.surgeryName = surgeryName; }

    public LocalDateTime getSurgeryDate() { return surgeryDate; }
    public void setSurgeryDate(LocalDateTime surgeryDate) { this.surgeryDate = surgeryDate; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
}
