package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.model.Dish;
import vn.travelist.repository.DishRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishRepository dishRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Dish>>> getAllDishes(
            @RequestParam(required = false) String keyword) {
        try {
            List<Dish> dishes;
            if (keyword != null && !keyword.trim().isEmpty()) {
                dishes = dishRepository.findByDishNameContainingIgnoreCase(keyword);
            } else {
                dishes = dishRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(dishes, "Lấy danh sách món ăn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách món ăn thất bại: " + e.getMessage(), "GET_DISHES_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Dish>> getDishById(@PathVariable Long id) {
        try {
            Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + id));
            return ResponseEntity.ok(ApiResponse.success(dish, "Lấy chi tiết món ăn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy món ăn: " + e.getMessage(), "DISH_NOT_FOUND")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Dish>> createDish(@Valid @RequestBody Dish dish) {
        try {
            Dish savedDish = dishRepository.save(dish);
            return ResponseEntity.ok(ApiResponse.success(savedDish, "Tạo món ăn mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo món ăn thất bại: " + e.getMessage(), "CREATE_DISH_FAILED")
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Dish>> updateDish(@PathVariable Long id, @Valid @RequestBody Dish dishDetails) {
        try {
            Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + id));

            dish.setDishName(dishDetails.getDishName());
            dish.setRestaurantName(dishDetails.getRestaurantName());
            dish.setAddress(dishDetails.getAddress());
            dish.setLatitude(dishDetails.getLatitude());
            dish.setLongitude(dishDetails.getLongitude());
            dish.setMinPrice(dishDetails.getMinPrice());
            dish.setMaxPrice(dishDetails.getMaxPrice());
            dish.setOpeningTime(dishDetails.getOpeningTime());
            dish.setClosingTime(dishDetails.getClosingTime());
            dish.setImageUrl(dishDetails.getImageUrl());

            Dish updatedDish = dishRepository.save(dish);
            return ResponseEntity.ok(ApiResponse.success(updatedDish, "Cập nhật món ăn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật món ăn thất bại: " + e.getMessage(), "UPDATE_DISH_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDish(@PathVariable Long id) {
        try {
            Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + id));
            dishRepository.delete(dish);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá món ăn thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá món ăn thất bại: " + e.getMessage(), "DELETE_DISH_FAILED")
            );
        }
    }
}
