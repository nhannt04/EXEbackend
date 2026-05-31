package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Stay;
import java.util.List;

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(String name, String type);
}
