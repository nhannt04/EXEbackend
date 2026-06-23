package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.DiaryReaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryReactionRepository extends JpaRepository<DiaryReaction, Long> {
    Optional<DiaryReaction> findByDiaryIdAndUserId(Long diaryId, Long userId);

    List<DiaryReaction> findByDiaryIdInAndUserId(Collection<Long> diaryIds, Long userId);
    void deleteByDiaryId(Long diaryId);
}
