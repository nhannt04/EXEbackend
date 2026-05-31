package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Entertainment;
import java.util.List;

@Repository
public interface EntertainmentRepository extends JpaRepository<Entertainment, Long> {
    List<Entertainment> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCaseOrInterestsContainingIgnoreCase(String name, String type, String interests);
}
