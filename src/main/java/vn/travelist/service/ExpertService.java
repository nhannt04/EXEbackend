package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.travelist.dto.ExpertInquiryRequest;
import vn.travelist.dto.ExpertInquiryResponse;
import vn.travelist.dto.ExpertResponse;
import vn.travelist.model.Expert;
import vn.travelist.model.ExpertInquiry;
import vn.travelist.model.User;
import vn.travelist.repository.ExpertInquiryRepository;
import vn.travelist.repository.ExpertRepository;
import vn.travelist.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertService {

    private final ExpertRepository expertRepository;
    private final ExpertInquiryRepository expertInquiryRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách các chuyên gia đang online hỗ trợ
     */
    @Transactional(readOnly = true)
    public List<ExpertResponse> getOnlineExperts() {
        return expertRepository.findByIsOnlineTrue().stream()
                .map(this::convertToExpertResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách toàn bộ chuyên gia bản địa
     */
    @Transactional(readOnly = true)
    public List<ExpertResponse> getAllExperts() {
        return expertRepository.findAll().stream()
                .map(this::convertToExpertResponse)
                .collect(Collectors.toList());
    }

    /**
     * Du khách gửi câu hỏi cho chuyên gia
     */
    @Transactional
    public ExpertInquiryResponse askQuestion(ExpertInquiryRequest request) {
        Expert expert = expertRepository.findById(request.getExpertId())
                .orElseThrow(() -> new RuntimeException("Chuyên gia không tồn tại!"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        ExpertInquiry inquiry = ExpertInquiry.builder()
                .expert(expert)
                .user(user)
                .question(request.getQuestion())
                .build();

        ExpertInquiry savedInquiry = expertInquiryRepository.save(inquiry);
        return convertToInquiryResponse(savedInquiry);
    }

    /**
     * Chuyên gia trả lời câu hỏi của du khách
     */
    @Transactional
    public ExpertInquiryResponse answerQuestion(Long inquiryId, String answer) {
        ExpertInquiry inquiry = expertInquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Phiên hỏi đáp không tồn tại!"));

        inquiry.setAnswer(answer);
        ExpertInquiry savedInquiry = expertInquiryRepository.save(inquiry);
        return convertToInquiryResponse(savedInquiry);
    }

    /**
     * Lấy lịch sử hỏi đáp gửi tới một Chuyên gia bản địa
     */
    @Transactional(readOnly = true)
    public List<ExpertInquiryResponse> getExpertInquiries(Long expertId) {
        return expertInquiryRepository.findByExpertIdOrderByCreatedAtDesc(expertId).stream()
                .map(this::convertToInquiryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử hỏi đáp của 1 du khách đã gửi đi
     */
    @Transactional(readOnly = true)
    public List<ExpertInquiryResponse> getUserInquiries(Long userId) {
        return expertInquiryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::convertToInquiryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper chuyển đổi Expert -> ExpertResponse
     */
    private ExpertResponse convertToExpertResponse(Expert expert) {
        return ExpertResponse.builder()
                .id(expert.getId())
                .expertise(expert.getExpertise())
                .descriptionVi(expert.getDescriptionVi())
                .descriptionEn(expert.getDescriptionEn())
                .isOnline(expert.getIsOnline())
                .rating(expert.getRating())
                .user(ExpertResponse.ProfileInfo.builder()
                        .id(expert.getUser().getId())
                        .fullName(expert.getUser().getFullName())
                        .email(expert.getUser().getEmail())
                        .avatarUrl(expert.getUser().getAvatarUrl())
                        .build())
                .build();
    }

    /**
     * Helper chuyển đổi ExpertInquiry -> ExpertInquiryResponse
     */
    private ExpertInquiryResponse convertToInquiryResponse(ExpertInquiry inquiry) {
        return ExpertInquiryResponse.builder()
                .id(inquiry.getId())
                .question(inquiry.getQuestion())
                .answer(inquiry.getAnswer())
                .createdAt(inquiry.getCreatedAt())
                .expertName(inquiry.getExpert().getUser().getFullName())
                .userName(inquiry.getUser().getFullName())
                .userAvatarUrl(inquiry.getUser().getAvatarUrl())
                .build();
    }
}
