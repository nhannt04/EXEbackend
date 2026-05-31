package vn.travelist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.travelist.model.Dish;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByDishNameContainingIgnoreCase(String dishName);
    List<Dish> findByRestaurantNameContainingIgnoreCase(String restaurantName);
}
