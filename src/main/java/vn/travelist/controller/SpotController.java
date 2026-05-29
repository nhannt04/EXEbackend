package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.SpotWithDistance;
import vn.travelist.model.Spot;
import vn.travelist.service.SpotService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/spots")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Spot>>> searchSpots(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        try {
            List<Spot> spots = spotService.searchSpots(category, keyword);
            return ResponseEntity.ok(ApiResponse.success(spots, "Lấy danh sách địa điểm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách địa điểm thất bại: " + e.getMessage(), "GET_SPOTS_FAILED")
            );
        }
    }

    /**
     * Lấy N địa điểm nổi bật ngẫu nhiên cho trang chủ (mặc định 8 spots)
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<Spot>>> getFeaturedSpots(
            @RequestParam(defaultValue = "8") int limit) {
        try {
            List<Spot> spots = spotService.getFeaturedSpots(limit);
            return ResponseEntity.ok(ApiResponse.success(spots, "Lấy địa điểm nổi bật thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy địa điểm nổi bật thất bại: " + e.getMessage(), "GET_FEATURED_FAILED")
            );
        }
    }

    /**
     * Lấy top spots theo danh mục cụ thể cho section phân loại trang chủ
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Spot>>> getTopByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "4") int limit) {
        try {
            List<Spot> spots = spotService.getTopByCategory(category, limit);
            return ResponseEntity.ok(ApiResponse.success(spots, "Lấy địa điểm theo danh mục thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy địa điểm theo danh mục thất bại: " + e.getMessage(), "GET_CATEGORY_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Spot>> getSpotById(@PathVariable Long id) {
        try {
            Spot spot = spotService.getSpotById(id);
            return ResponseEntity.ok(ApiResponse.success(spot, "Lấy chi tiết địa điểm thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy địa điểm: " + e.getMessage(), "SPOT_NOT_FOUND")
            );
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<SpotWithDistance>>> getNearbySpots(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {
        try {
            List<SpotWithDistance> nearbySpots = spotService.getNearbySpots(lat, lng, radius);
            return ResponseEntity.ok(ApiResponse.success(nearbySpots, "Tìm kiếm các địa điểm lân cận thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Tìm kiếm địa điểm lân cận thất bại: " + e.getMessage(), "GET_NEARBY_SPOTS_FAILED")
            );
        }
    }

    /**
     * Tạo địa điểm du lịch mới
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Spot>> createSpot(@Valid @RequestBody Spot spot) {
        try {
            Spot savedSpot = spotService.createSpot(spot);
            return ResponseEntity.ok(ApiResponse.success(savedSpot, "Đăng ký địa điểm du lịch thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Đăng ký địa điểm thất bại: " + e.getMessage(), "CREATE_SPOT_FAILED")
            );
        }
    }

    /**
     * Xoá địa điểm du lịch theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSpot(@PathVariable Long id) {
        try {
            spotService.deleteSpot(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá địa điểm du lịch thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá địa điểm thất bại: " + e.getMessage(), "DELETE_SPOT_FAILED")
            );
        }
    }

    /**
     * Cập nhật thông tin địa điểm du lịch
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Spot>> updateSpot(@PathVariable Long id, @Valid @RequestBody Spot spot) {
        try {
            Spot updatedSpot = spotService.updateSpot(id, spot);
            return ResponseEntity.ok(ApiResponse.success(updatedSpot, "Cập nhật địa điểm du lịch thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật địa điểm thất bại: " + e.getMessage(), "UPDATE_SPOT_FAILED")
            );
        }
    }
}
