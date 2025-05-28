package  rw.rra.vms.demo.Repositories;

import rw.rra.vms.demo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by email.
     * @param email The user's email.
     * @return Optional containing the user, if found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by national ID.
     * @param nationalId The user's national ID.
     * @return Optional containing the user, if found.
     */
    Optional<User> findByNationalId(String nationalId);
}