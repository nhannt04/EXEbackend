package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.model.Stay;
import vn.travelist.repository.StayRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stays")
@RequiredArgsConstructor
public class StayController {

    private final StayRepository stayRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Stay>>> getAllStays(
            @RequestParam(required = false) String keyword) {
        try {
            List<Stay> stays;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stays = stayRepository.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(keyword, keyword);
            } else {
                stays = stayRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(stays, "Lấy danh sách chỗ ở thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách chỗ ở thất bại: " + e.getMessage(), "GET_STAYS_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Stay>> getStayById(@PathVariable Long id) {
        try {
            Stay stay = stayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chỗ ở không tồn tại với ID: " + id));
            return ResponseEntity.ok(ApiResponse.success(stay, "Lấy chi tiết chỗ ở thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy chỗ ở: " + e.getMessage(), "STAY_NOT_FOUND")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Stay>> createStay(@Valid @RequestBody Stay stay) {
        try {
            Stay savedStay = stayRepository.save(stay);
            return ResponseEntity.ok(ApiResponse.success(savedStay, "Tạo chỗ ở mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo chỗ ở thất bại: " + e.getMessage(), "CREATE_STAY_FAILED")
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Stay>> updateStay(@PathVariable Long id, @Valid @RequestBody Stay stayDetails) {
        try {
            Stay stay = stayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chỗ ở không tồn tại với ID: " + id));

            stay.setType(stayDetails.getType());
            stay.setName(stayDetails.getName());
            stay.setAddress(stayDetails.getAddress());
            stay.setLatitude(stayDetails.getLatitude());
            stay.setLongitude(stayDetails.getLongitude());
            stay.setCapacity(stayDetails.getCapacity());
            stay.setMinPrice(stayDetails.getMinPrice());
            stay.setMaxPrice(stayDetails.getMaxPrice());
            stay.setNotes(stayDetails.getNotes());
            stay.setImageUrl(stayDetails.getImageUrl());

            Stay updatedStay = stayRepository.save(stay);
            return ResponseEntity.ok(ApiResponse.success(updatedStay, "Cập nhật chỗ ở thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật chỗ ở thất bại: " + e.getMessage(), "UPDATE_STAY_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStay(@PathVariable Long id) {
        try {
            Stay stay = stayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chỗ ở không tồn tại với ID: " + id));
            stayRepository.delete(stay);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá chỗ ở thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá chỗ ở thất bại: " + e.getMessage(), "DELETE_STAY_FAILED")
            );
        }
    }
}
