package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Stay;
import java.util.List;

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {
    @Query("SELECT DISTINCT s.type FROM Stay s WHERE s.type IS NOT NULL")
    List<String> findDistinctStayTypes();

    List<Stay> findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(String name, String type);
}
