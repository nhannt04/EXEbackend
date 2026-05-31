package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.EntertainmentStatusResponse;
import vn.travelist.model.Entertainment;
import vn.travelist.repository.EntertainmentRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/operating/now")
    public ResponseEntity<ApiResponse<List<Entertainment>>> getOperatingEntertainments() {
        try {
            LocalTime now = LocalTime.now();
            List<Entertainment> allEntertainments = entertainmentRepository.findAll();
            
            List<Entertainment> operatingEntertainments = allEntertainments.stream()
                .filter(ent -> {
                    if (ent.getOpeningTime() == null || ent.getClosingTime() == null) {
                        return false; // Skip if no hours set
                    }
                    boolean overnight = Boolean.TRUE.equals(ent.getOvernight()) || ent.getOpeningTime().isAfter(ent.getClosingTime());
                    if (overnight) {
                        // For overnight: open if after opening time OR before closing time
                        return !now.isBefore(ent.getOpeningTime()) || !now.isAfter(ent.getClosingTime());
                    } else {
                        // Normal hours: open if between opening and closing
                        return !now.isBefore(ent.getOpeningTime()) && !now.isAfter(ent.getClosingTime());
                    }
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(operatingEntertainments, "Lấy danh sách khu vui chơi đang mở cửa thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách khu vui chơi đang mở cửa thất bại: " + e.getMessage(), "GET_OPERATING_ENTERTAINMENTS_FAILED")
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
            // Normalize overnight flag so schedules like 20:00 -> 02:00 are stored consistently
            if (entertainment.getOpeningTime() != null && entertainment.getClosingTime() != null
                    && entertainment.getOpeningTime().isAfter(entertainment.getClosingTime())) {
                entertainment.setOvernight(true);
            } else if (entertainment.getOvernight() == null) {
                entertainment.setOvernight(false);
            }
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
            // Set opening/closing times if provided
            entertainment.setOpeningTime(entertainmentDetails.getOpeningTime());
            entertainment.setClosingTime(entertainmentDetails.getClosingTime());
            if (entertainmentDetails.getOpeningTime() != null && entertainmentDetails.getClosingTime() != null
                    && entertainmentDetails.getOpeningTime().isAfter(entertainmentDetails.getClosingTime())) {
                entertainment.setOvernight(true);
            } else if (entertainmentDetails.getOvernight() != null) {
                entertainment.setOvernight(entertainmentDetails.getOvernight());
            }

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

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EntertainmentStatusResponse>> getEntertainmentStatus(@PathVariable Long id) {
        try {
            Entertainment entertainment = entertainmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khu vui chơi không tồn tại với ID: " + id));

            LocalTime now = LocalTime.now();
            LocalTime opening = entertainment.getOpeningTime();
            LocalTime closing = entertainment.getClosingTime();
            boolean overnight = Boolean.TRUE.equals(entertainment.getOvernight());

            boolean open;
            String message;

            if (opening == null || closing == null) {
                open = false;
                message = "Chưa có dữ liệu giờ hoạt động";
            } else if (overnight || opening.isAfter(closing)) {
                open = !now.isBefore(opening) || !now.isAfter(closing);
                message = open ? "Đang mở cửa (qua đêm)" : "Đang đóng cửa";
            } else {
                open = !now.isBefore(opening) && !now.isAfter(closing);
                message = open ? "Đang mở cửa" : "Đang đóng cửa";
            }

            EntertainmentStatusResponse body = EntertainmentStatusResponse.builder()
                .id(entertainment.getId())
                .name(entertainment.getName())
                .open(open)
                .overnight(overnight)
                .openingTime(opening)
                .closingTime(closing)
                .message(message)
                .build();

            return ResponseEntity.ok(ApiResponse.success(body, "Lấy trạng thái khu vui chơi thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không lấy được trạng thái khu vui chơi: " + e.getMessage(), "ENTERTAINMENT_STATUS_FAILED")
            );
        }
    }
}
