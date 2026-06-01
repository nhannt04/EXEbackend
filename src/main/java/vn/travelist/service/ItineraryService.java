package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.travelist.dto.ItinerarySaveRequest;
import vn.travelist.dto.ItineraryResponse;
import vn.travelist.model.Itinerary;
import vn.travelist.model.User;
import vn.travelist.repository.ItineraryRepository;
import vn.travelist.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;

    public ItineraryResponse saveItinerary(ItinerarySaveRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Itinerary itinerary = Itinerary.builder()
                .user(user)
                .title(request.getTitle())
                .totalDays(request.getTotalDays())
                .totalBudget(request.getTotalBudget())
                .travelStyle(request.getTravelStyle())
                .groupType(request.getGroupType())
                .tripData(request.getTripData())
                .build();

        Itinerary saved = itineraryRepository.save(itinerary);
        return mapToResponse(saved);
    }

    public List<ItineraryResponse> getMyItineraries(Long userId) {
        return itineraryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ItineraryResponse> getCompletedItineraries(Long userId) {
        return itineraryRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteItinerary(Long id, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));
        
        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa lịch trình này!");
        }

        itineraryRepository.delete(itinerary);
    }

    public ItineraryResponse updateItineraryStatus(Long id, String status, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));

        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền cập nhật lịch trình này!");
        }

        itinerary.setStatus(status);
        Itinerary saved = itineraryRepository.save(itinerary);
        return mapToResponse(saved);
    }

    private ItineraryResponse mapToResponse(Itinerary itinerary) {
        return ItineraryResponse.builder()
                .id(itinerary.getId())
                .title(itinerary.getTitle())
                .destination(itinerary.getDestination())
                .totalDays(itinerary.getTotalDays())
                .totalBudget(itinerary.getTotalBudget())
                .travelStyle(itinerary.getTravelStyle())
                .groupType(itinerary.getGroupType())
                .tripData(itinerary.getTripData())
                .status(itinerary.getStatus())
                .createdAt(itinerary.getCreatedAt())
                .build();
    }
}
