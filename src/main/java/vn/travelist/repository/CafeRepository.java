package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Cafe;
import java.util.List;

@Repository
public interface CafeRepository extends JpaRepository<Cafe, Long> {
    List<Cafe> findByNameContainingIgnoreCaseOrStyleContainingIgnoreCase(String name, String style);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT c.style FROM Cafe c WHERE c.style IS NOT NULL AND TRIM(c.style) != ''")
    List<String> findDistinctStyles();
}
