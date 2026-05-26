package vn.histra.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.histra.dto.ApiResponse;
import vn.histra.dto.TripRequest;
import vn.histra.dto.TripResponse;
import vn.histra.service.TripService;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Hỗ trợ kết nối thông suốt từ mọi địa chỉ frontend
public class TripController {

    private final TripService tripService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<TripResponse>> generateTrip(@RequestBody TripRequest request) {
        try {
            TripResponse response = tripService.generateItinerary(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Sinh lịch trình du lịch tối ưu thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Không thể tự động sinh lịch trình: " + e.getMessage(), "ITINERARY_GENERATION_FAILED")
            );
        }
    }
}
