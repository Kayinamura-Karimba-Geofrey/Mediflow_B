package geofrey.project.Mediflow_B.repository;

import geofrey.project.Mediflow_B.entity.Role;
import geofrey.project.Mediflow_B.entity.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
