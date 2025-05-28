package rw.rra.vms.demo.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.rra.vms.demo.Entities.PlateNumber;
import rw.rra.vms.demo.Entities.VehicleOwner;

import java.util.List;
import java.util.Optional;

public interface PlateNumberRepository extends JpaRepository<PlateNumber, Long> {
    List<PlateNumber> findByOwner(VehicleOwner owner);
    Optional<PlateNumber> findByPlateNumber(String plateNumber);
}
