package vn.travelist.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.dto.ItinerarySaveRequest;
import vn.travelist.dto.ItineraryResponse;
import vn.travelist.security.JwtTokenProvider;
import vn.travelist.service.ItineraryService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponse<ItineraryResponse>> saveItinerary(
            @RequestBody ItinerarySaveRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để lưu lịch trình!", "UNAUTHORIZED")
                );
            }

            ItineraryResponse response = itineraryService.saveItinerary(request, userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Lưu lịch trình du lịch thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lưu lịch trình thất bại: " + e.getMessage(), "SAVE_ITINERARY_FAILED")
            );
        }
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getMyItineraries(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để xem lịch trình của tôi!", "UNAUTHORIZED")
                );
            }

            List<ItineraryResponse> list = itineraryService.getMyItineraries(userId);
            return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách lịch trình đã lưu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lấy danh sách lịch trình thất bại: " + e.getMessage(), "GET_ITINERARIES_FAILED")
            );
        }
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getCompletedItineraries(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để xem lịch trình đã hoàn thành!", "UNAUTHORIZED")
                );
            }

            List<ItineraryResponse> list = itineraryService.getCompletedItineraries(userId);
            return ResponseEntity.ok(ApiResponse.success(list, "Lấy danh sách lịch trình đã hoàn thành thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Lấy danh sách lịch trình đã hoàn thành thất bại: " + e.getMessage(), "GET_COMPLETED_ITINERARIES_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItinerary(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để xóa lịch trình này!", "UNAUTHORIZED")
                );
            }

            itineraryService.deleteItinerary(id, userId);
            return ResponseEntity.ok(ApiResponse.success(null, "Xóa lịch trình thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xóa lịch trình thất bại: " + e.getMessage(), "DELETE_ITINERARY_FAILED")
            );
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ItineraryResponse>> updateItineraryStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để cập nhật trạng thái lịch trình!", "UNAUTHORIZED")
                );
            }

            ItineraryResponse response = itineraryService.updateItineraryStatus(id, status, userId);
            return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái lịch trình thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật trạng thái thất bại: " + e.getMessage(), "UPDATE_STATUS_FAILED")
            );
        }
    }

    @GetMapping("/{id}/handbook")
    public ResponseEntity<ApiResponse<String>> getItineraryHandbook(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromHeader(authHeader);
            if (userId == null) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Bạn cần đăng nhập để tạo cẩm nang!", "UNAUTHORIZED")
                );
            }

            String handbookJson = itineraryService.generateHandbookForItinerary(id, userId);
            if (handbookJson.startsWith("{\"error\"")) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(handbookJson, "GENERATE_HANDBOOK_FAILED")
                );
            }
            return ResponseEntity.ok(ApiResponse.success(handbookJson, "Tạo cẩm nang từ AI thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo cẩm nang thất bại: " + e.getMessage(), "GENERATE_HANDBOOK_FAILED")
            );
        }
    }

    private Long getUserIdFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromJWT(token);
            }
        }
        return null;
    }
}
