package geofrey.project.Mediflow_B.repository;

import geofrey.project.Mediflow_B.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
