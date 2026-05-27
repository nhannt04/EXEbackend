package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.histra.dto.ItinerarySaveRequest;
import vn.histra.dto.ItineraryResponse;
import vn.histra.model.Itinerary;
import vn.histra.model.User;
import vn.histra.repository.ItineraryRepository;
import vn.histra.repository.UserRepository;
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

    public void deleteItinerary(Long id, Long userId) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch trình không tồn tại!"));
        
        if (!itinerary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa lịch trình này!");
        }

        itineraryRepository.delete(itinerary);
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
                .createdAt(itinerary.getCreatedAt())
                .build();
    }
}
