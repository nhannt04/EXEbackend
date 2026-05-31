package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Rental;
import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(String name, String type);
}
