package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Dish;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    @Query("SELECT DISTINCT d.dishName FROM Dish d WHERE d.dishName IS NOT NULL")
    List<String> findDistinctDishNames();

    List<Dish> findByDishNameContainingIgnoreCase(String dishName);
    List<Dish> findByRestaurantNameContainingIgnoreCase(String restaurantName);
}
