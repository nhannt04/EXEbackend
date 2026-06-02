package vn.travelist.config;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class CacheEvictAspect {

    private final CacheManager cacheManager;

    @AfterReturning(pointcut = "(execution(* vn.travelist.repository.StayRepository.save*(..)) || execution(* vn.travelist.repository.StayRepository.delete*(..))) " +
            "|| (execution(* vn.travelist.repository.CafeRepository.save*(..)) || execution(* vn.travelist.repository.CafeRepository.delete*(..))) " +
            "|| (execution(* vn.travelist.repository.DishRepository.save*(..)) || execution(* vn.travelist.repository.DishRepository.delete*(..))) " +
            "|| (execution(* vn.travelist.repository.EntertainmentRepository.save*(..)) || execution(* vn.travelist.repository.EntertainmentRepository.delete*(..))) " +
            "|| (execution(* vn.travelist.repository.RentalRepository.save*(..)) || execution(* vn.travelist.repository.RentalRepository.delete*(..)))")
    public void evictSpotCaches() {
        if (cacheManager.getCache("allVirtualSpots") != null) {
            cacheManager.getCache("allVirtualSpots").clear();
        }
        if (cacheManager.getCache("spotById") != null) {
            cacheManager.getCache("spotById").clear();
        }
        if (cacheManager.getCache("generatedItineraries") != null) {
            cacheManager.getCache("generatedItineraries").clear();
        }
    }

    @AfterReturning(pointcut = "execution(* vn.travelist.repository.ExpertRepository.save*(..)) || execution(* vn.travelist.repository.ExpertRepository.delete*(..))")
    public void evictExpertCaches() {
        if (cacheManager.getCache("experts") != null) {
            cacheManager.getCache("experts").clear();
        }
    }
}
