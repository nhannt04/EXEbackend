package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.model.Entertainment;
import vn.travelist.repository.EntertainmentRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/entertainments")
@RequiredArgsConstructor
public class EntertainmentController {

    private final EntertainmentRepository entertainmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Entertainment>>> getAllEntertainments(
            @RequestParam(required = false) String keyword) {
        try {
            List<Entertainment> entertainments;
            if (keyword != null && !keyword.trim().isEmpty()) {
                entertainments = entertainmentRepository.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCaseOrInterestsContainingIgnoreCase(keyword, keyword, keyword);
            } else {
                entertainments = entertainmentRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(entertainments, "Lấy danh sách khu vui chơi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách khu vui chơi thất bại: " + e.getMessage(), "GET_ENTERTAINMENTS_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Entertainment>> getEntertainmentById(@PathVariable Long id) {
        try {
            Entertainment entertainment = entertainmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vui chơi không tồn tại với ID: " + id));
            return ResponseEntity.ok(ApiResponse.success(entertainment, "Lấy chi tiết khu vui chơi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy khu vui chơi: " + e.getMessage(), "ENTERTAINMENT_NOT_FOUND")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Entertainment>> createEntertainment(@Valid @RequestBody Entertainment entertainment) {
        try {
            Entertainment savedEntertainment = entertainmentRepository.save(entertainment);
            return ResponseEntity.ok(ApiResponse.success(savedEntertainment, "Tạo khu vui chơi mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo khu vui chơi thất bại: " + e.getMessage(), "CREATE_ENTERTAINMENT_FAILED")
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Entertainment>> updateEntertainment(@PathVariable Long id, @Valid @RequestBody Entertainment entertainmentDetails) {
        try {
            Entertainment entertainment = entertainmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vui chơi không tồn tại với ID: " + id));

            entertainment.setType(entertainmentDetails.getType());
            entertainment.setInterests(entertainmentDetails.getInterests());
            entertainment.setName(entertainmentDetails.getName());
            entertainment.setAddress(entertainmentDetails.getAddress());
            entertainment.setLatitude(entertainmentDetails.getLatitude());
            entertainment.setLongitude(entertainmentDetails.getLongitude());
            entertainment.setMinPrice(entertainmentDetails.getMinPrice());
            entertainment.setMaxPrice(entertainmentDetails.getMaxPrice());
            entertainment.setImageUrl(entertainmentDetails.getImageUrl());

            Entertainment updatedEntertainment = entertainmentRepository.save(entertainment);
            return ResponseEntity.ok(ApiResponse.success(updatedEntertainment, "Cập nhật khu vui chơi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật khu vui chơi thất bại: " + e.getMessage(), "UPDATE_ENTERTAINMENT_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntertainment(@PathVariable Long id) {
        try {
            Entertainment entertainment = entertainmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vui chơi không tồn tại với ID: " + id));
            entertainmentRepository.delete(entertainment);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá khu vui chơi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá khu vui chơi thất bại: " + e.getMessage(), "DELETE_ENTERTAINMENT_FAILED")
            );
        }
    }
}
