package geofrey.project.Mediflow_B.config;

import geofrey.project.Mediflow_B.entity.*;
import geofrey.project.Mediflow_B.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private NurseRepository nurseRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRecordRepository patientRecordRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedUsers();
        seedDepartmentsAndStaff();
        seedPatientsAndRecords();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            roleRepository.save(new Role(ERole.ROLE_DOCTOR));
            roleRepository.save(new Role(ERole.ROLE_NURSE));
            roleRepository.save(new Role(ERole.ROLE_PATIENT));
            roleRepository.save(new Role(ERole.ROLE_RECEPTIONIST));
            System.out.println("Seeded Roles.");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            String encodedPassword = passwordEncoder.encode("password123");

            // Admin
            User admin = new User("admin", "admin@mediflow.com", encodedPassword, "System Administrator");
            admin.setEnabled(true);
            admin.setDob(LocalDate.of(1985, 5, 20));
            admin.setAge(Period.between(admin.getDob(), LocalDate.now()).getYears());
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
            admin.setRoles(adminRoles);
            userRepository.save(admin);

            // Doctor User
            User docUser = new User("doctor_user", "doctor@mediflow.com", encodedPassword, "Dr. John Smith");
            docUser.setEnabled(true);
            docUser.setDob(LocalDate.of(1978, 11, 12));
            docUser.setAge(Period.between(docUser.getDob(), LocalDate.now()).getYears());
            Set<Role> docRoles = new HashSet<>();
            docRoles.add(roleRepository.findByName(ERole.ROLE_DOCTOR).get());
            docUser.setRoles(docRoles);
            userRepository.save(docUser);

            // Nurse User
            User nurseUser = new User("nurse_user", "nurse@mediflow.com", encodedPassword, "Nurse Mary Jane");
            nurseUser.setEnabled(true);
            nurseUser.setDob(LocalDate.of(1992, 3, 15));
            nurseUser.setAge(Period.between(nurseUser.getDob(), LocalDate.now()).getYears());
            Set<Role> nurseRoles = new HashSet<>();
            nurseRoles.add(roleRepository.findByName(ERole.ROLE_NURSE).get());
            nurseUser.setRoles(nurseRoles);
            userRepository.save(nurseUser);

            // Patient User
            User patientUser = new User("patient_user", "patient@mediflow.com", encodedPassword, "Jane Doe");
            patientUser.setEnabled(true);
            patientUser.setDob(LocalDate.of(1995, 7, 25));
            patientUser.setAge(Period.between(patientUser.getDob(), LocalDate.now()).getYears());
            Set<Role> patRoles = new HashSet<>();
            patRoles.add(roleRepository.findByName(ERole.ROLE_USER).get());
            patientUser.setRoles(patRoles);
            userRepository.save(patientUser);

            System.out.println("Seeded Users.");
        }
    }

    private void seedDepartmentsAndStaff() {
        if (departmentRepository.count() == 0) {
            Department cardio = new Department();
            cardio.setName("Cardiology");
            cardio.setDescription("Heart and blood vessel department");

            Department neuro = new Department();
            neuro.setName("Neurology");
            neuro.setDescription("Brain and nervous system department");

            departmentRepository.save(cardio);
            departmentRepository.save(neuro);

            Doctor doc1 = new Doctor();
            doc1.setName("Dr. John Smith");
            doc1.setSpecialisation("Cardiologist");
            doc1.setEmail("john.smith@mediflow.com");
            doc1.setPhone("1234567890");
            doc1.setDepartment(cardio);
            doctorRepository.save(doc1);

            Doctor doc2 = new Doctor();
            doc2.setName("Dr. Sarah Wilson");
            doc2.setSpecialisation("Neurologist");
            doc2.setEmail("sarah.wilson@mediflow.com");
            doc2.setPhone("0987654321");
            doc2.setDepartment(neuro);
            doctorRepository.save(doc2);

            Nurse nurse = new Nurse();
            nurse.setName("Nurse Mary Jane");
            nurse.setEmail("mary.jane@mediflow.com");
            nurse.setPhone("1122334455");
            nurse.setQualification("Senior Nurse");
            nurseRepository.save(nurse);

            System.out.println("Seeded Departments, Doctors, and Nurses.");
        }
    }

    private void seedPatientsAndRecords() {
        if (patientRepository.count() == 0) {
            Doctor doc = doctorRepository.findAll().get(0);
            Nurse nurse = nurseRepository.findAll().get(0);
            User patientUser = userRepository.findByUsername("patient_user").get();

            Patient patient = new Patient();
            patient.setName("Jane Doe");
            patient.setDisease("Hypertension");
            patient.setEmail("jane.doe@example.com");
            patient.setPhone("555-1234");
            patient.setDoctor(doc);
            patient.setNurse(nurse);
            patient.setUser(patientUser);
            patientRepository.save(patient);

            Appointment appt = new Appointment();
            appt.setPatient(patient);
            appt.setDoctor(doc);
            appt.setAppointmentDate(LocalDateTime.now().plusDays(2));
            appt.setReason("Regular checkup");
            appt.setStatus("CONFIRMED");
            appointmentRepository.save(appt);

            PatientRecord record = new PatientRecord();
            record.setPatient(patient);
            record.setDoctor(doc);
            record.setDiagnosis("Mild Hypertension");
            record.setTreatmentPlan("Reduced salt intake and daily exercise");
            record.setRecordedAt(LocalDateTime.now());
            patientRecordRepository.save(record);

            Prescription prescription = new Prescription();
            prescription.setPatient(patient);
            prescription.setDoctor(doc);
            prescription.setMedicineName("Lisinopril");
            prescription.setDosage("10mg");
            prescription.setFrequency("Once daily");
            prescription.setDuration("30 days");
            prescription.setPrescribedDate(LocalDateTime.now());
            prescriptionRepository.save(prescription);

            System.out.println("Seeded Patients, Appointments, and Records.");
        }
    }
}
