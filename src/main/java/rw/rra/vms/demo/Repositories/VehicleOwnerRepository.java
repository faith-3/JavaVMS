package rw.rra.vms.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.rra.vms.demo.Entities.VehicleOwner;

import java.util.Optional;

public interface VehicleOwnerRepository extends JpaRepository<VehicleOwner, Long> {
    Optional<VehicleOwner> findByNationalId(String nationalId);
    Optional<VehicleOwner> findByPhone(String phone);
    Optional<VehicleOwner> findByEmail(String email);
}
