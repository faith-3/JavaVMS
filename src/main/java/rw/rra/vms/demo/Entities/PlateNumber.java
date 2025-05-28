package rw.rra.vms.demo.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "plate_number")
public class PlateNumber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private VehicleOwner owner;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private LocalDate issuedDate;

    private boolean inUse;
}