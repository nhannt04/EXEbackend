package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.model.Cafe;
import vn.travelist.repository.CafeRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeRepository cafeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Cafe>>> getAllCafes(
            @RequestParam(required = false) String keyword) {
        try {
            List<Cafe> cafes;
            if (keyword != null && !keyword.trim().isEmpty()) {
                cafes = cafeRepository.findByNameContainingIgnoreCaseOrStyleContainingIgnoreCase(keyword, keyword);
            } else {
                cafes = cafeRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(cafes, "Lấy danh sách quán cà phê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách quán cà phê thất bại: " + e.getMessage(), "GET_CAFES_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Cafe>> getCafeById(@PathVariable Long id) {
        try {
            Cafe cafe = cafeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quán cà phê không tồn tại với ID: " + id));
            return ResponseEntity.ok(ApiResponse.success(cafe, "Lấy chi tiết quán cà phê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy quán cà phê: " + e.getMessage(), "CAFE_NOT_FOUND")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Cafe>> createCafe(@Valid @RequestBody Cafe cafe) {
        try {
            Cafe savedCafe = cafeRepository.save(cafe);
            return ResponseEntity.ok(ApiResponse.success(savedCafe, "Tạo quán cà phê mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo quán cà phê thất bại: " + e.getMessage(), "CREATE_CAFE_FAILED")
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Cafe>> updateCafe(@PathVariable Long id, @Valid @RequestBody Cafe cafeDetails) {
        try {
            Cafe cafe = cafeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quán cà phê không tồn tại với ID: " + id));

            cafe.setName(cafeDetails.getName());
            cafe.setStyle(cafeDetails.getStyle());
            cafe.setAddress(cafeDetails.getAddress());
            cafe.setLatitude(cafeDetails.getLatitude());
            cafe.setLongitude(cafeDetails.getLongitude());
            cafe.setMinPrice(cafeDetails.getMinPrice());
            cafe.setMaxPrice(cafeDetails.getMaxPrice());
            cafe.setOpeningTime(cafeDetails.getOpeningTime());
            cafe.setClosingTime(cafeDetails.getClosingTime());
            cafe.setImageUrl(cafeDetails.getImageUrl());

            Cafe updatedCafe = cafeRepository.save(cafe);
            return ResponseEntity.ok(ApiResponse.success(updatedCafe, "Cập nhật quán cà phê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật quán cà phê thất bại: " + e.getMessage(), "UPDATE_CAFE_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCafe(@PathVariable Long id) {
        try {
            Cafe cafe = cafeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quán cà phê không tồn tại với ID: " + id));
            cafeRepository.delete(cafe);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá quán cà phê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá quán cà phê thất bại: " + e.getMessage(), "DELETE_CAFE_FAILED")
            );
        }
    }
}
