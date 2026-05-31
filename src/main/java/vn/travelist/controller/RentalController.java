package vn.travelist.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.travelist.dto.ApiResponse;
import vn.travelist.model.Rental;
import vn.travelist.repository.RentalRepository;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalRepository rentalRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Rental>>> getAllRentals(
            @RequestParam(required = false) String keyword) {
        try {
            List<Rental> rentals;
            if (keyword != null && !keyword.trim().isEmpty()) {
                rentals = rentalRepository.findByNameContainingIgnoreCaseOrTypeContainingIgnoreCase(keyword, keyword);
            } else {
                rentals = rentalRepository.findAll();
            }
            return ResponseEntity.ok(ApiResponse.success(rentals, "Lấy danh sách dịch vụ cho thuê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Lấy danh sách dịch vụ cho thuê thất bại: " + e.getMessage(), "GET_RENTALS_FAILED")
            );
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Rental>> getRentalById(@PathVariable Long id) {
        try {
            Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ cho thuê không tồn tại với ID: " + id));
            return ResponseEntity.ok(ApiResponse.success(rental, "Lấy chi tiết dịch vụ cho thuê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Không tìm thấy dịch vụ cho thuê: " + e.getMessage(), "RENTAL_NOT_FOUND")
            );
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Rental>> createRental(@Valid @RequestBody Rental rental) {
        try {
            Rental savedRental = rentalRepository.save(rental);
            return ResponseEntity.ok(ApiResponse.success(savedRental, "Tạo dịch vụ cho thuê mới thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Tạo dịch vụ cho thuê thất bại: " + e.getMessage(), "CREATE_RENTAL_FAILED")
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Rental>> updateRental(@PathVariable Long id, @Valid @RequestBody Rental rentalDetails) {
        try {
            Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ cho thuê không tồn tại với ID: " + id));

            rental.setType(rentalDetails.getType());
            rental.setName(rentalDetails.getName());
            rental.setAddress(rentalDetails.getAddress());
            rental.setLatitude(rentalDetails.getLatitude());
            rental.setLongitude(rentalDetails.getLongitude());
            rental.setMinPrice(rentalDetails.getMinPrice());
            rental.setMaxPrice(rentalDetails.getMaxPrice());
            rental.setOpeningTime(rentalDetails.getOpeningTime());
            rental.setClosingTime(rentalDetails.getClosingTime());
            rental.setImageUrl(rentalDetails.getImageUrl());

            Rental updatedRental = rentalRepository.save(rental);
            return ResponseEntity.ok(ApiResponse.success(updatedRental, "Cập nhật dịch vụ cho thuê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cập nhật dịch vụ cho thuê thất bại: " + e.getMessage(), "UPDATE_RENTAL_FAILED")
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRental(@PathVariable Long id) {
        try {
            Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ cho thuê không tồn tại với ID: " + id));
            rentalRepository.delete(rental);
            return ResponseEntity.ok(ApiResponse.success(null, "Xoá dịch vụ cho thuê thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Xoá dịch vụ cho thuê thất bại: " + e.getMessage(), "DELETE_RENTAL_FAILED")
            );
        }
    }
}
