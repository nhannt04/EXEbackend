package vn.histra.service;

import org.springframework.stereotype.Service;
import vn.histra.model.Spot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RouteOptimizationService {

    public List<Spot> optimizeRoute(List<Spot> spots, double startLat, double startLng) {
        List<Spot> result = new ArrayList<>();
        List<Spot> remaining = new ArrayList<>(spots);
        
        double currentLat = startLat;
        double currentLng = startLng;

        while (!remaining.isEmpty()) {
            final double tempLat = currentLat;
            final double tempLng = currentLng;
            
            // Tìm địa điểm gần vị trí hiện tại nhất theo công thức Haversine
            Spot nearest = remaining.stream()
                .min(Comparator.comparingDouble(spot -> 
                    haversine(tempLat, tempLng, spot.getLatitude(), spot.getLongitude())))
                .orElseThrow();

            result.add(nearest);
            remaining.remove(nearest);
            
            // Di chuyển vị trí hiện tại tới tọa độ của điểm vừa được chọn
            currentLat = nearest.getLatitude();
            currentLng = nearest.getLongitude();
        }
        return result;
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371; // Bán kính trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
