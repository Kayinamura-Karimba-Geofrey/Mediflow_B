package geofrey.project.Mediflow_B.repository;

import geofrey.project.Mediflow_B.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialisation(String specialisation);
    List<Doctor> findByDepartmentId(Long departmentId);
}
