package vn.travelist.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.travelist.dto.CommentResponse;
import vn.travelist.dto.DiaryRequest;
import vn.travelist.dto.DiaryResponse;
import vn.travelist.model.Comment;
import vn.travelist.model.Diary;
import vn.travelist.model.DiaryImage;
import vn.travelist.model.User;
import vn.travelist.repository.CommentRepository;
import vn.travelist.repository.DiaryRepository;
import vn.travelist.repository.ItineraryRepository;
import vn.travelist.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final SpotService spotService;
    private final CloudflareImageService cloudflareImageService;

    /**
     * Lấy toàn bộ nhật ký (có thể lọc theo chuyên mục) kèm tác giả và danh sách bình luận
     */
    @Transactional(readOnly = true)
    public List<DiaryResponse> getAllDiaries(String category) {
        List<Diary> diaries;
        if (category == null || category.isEmpty() || "all".equalsIgnoreCase(category)) {
            diaries = diaryRepository.findAllByOrderByCreatedAtDesc();
        } else {
            diaries = diaryRepository.findByCategoryOrderByCreatedAtDesc(category);
        }

        return diaries.stream()
                .map(this::convertToDiaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo bài viết nhật ký du ký mới kèm tệp ảnh upload lên Cloudflare (Fallback cho Multipart)
     */
    @Transactional
    public DiaryResponse createDiary(Long userId, String category, String contentVi, String contentEn, Long spotId, Long itineraryId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra ràng buộc: mỗi địa điểm trong một lịch trình chỉ được đăng 1 lần
        if (spotId != null && itineraryId != null) {
            boolean alreadyPosted = diaryRepository.existsByUserIdAndSpotIdAndItineraryId(userId, spotId, itineraryId);
            if (alreadyPosted) {
                throw new RuntimeException("Địa điểm này đã được đăng trong lịch trình này. Hãy hoàn thành một lịch trình khác để đăng lại!");
            }
        }

        String imageCfId = null;
        String imageUrl = null;

        // Tiến hành upload ảnh lên Cloudflare nếu người dùng chọn gửi tệp ảnh đính kèm
        if (imageFile != null && !imageFile.isEmpty()) {
            Map<String, String> uploadResult = cloudflareImageService.uploadImage(imageFile);
            imageCfId = uploadResult.get("cfId");
            imageUrl = uploadResult.get("url");
        } else {
            // Ảnh Hội An phong cảnh mặc định nếu không chọn ảnh
            imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=600&q=80";
        }

        Diary diary = Diary.builder()
                .user(user)
                .category(category)
                .contentVi(contentVi)
                .contentEn(contentEn)
                .spotId(spotId)
                .itineraryId(itineraryId)
                .build();

        List<DiaryImage> diaryImages = new ArrayList<>();
        if (imageUrl != null) {
            diaryImages.add(DiaryImage.builder()
                    .diary(diary)
                    .imageCfId(imageCfId)
                    .imageUrl(imageUrl)
                    .build());
        }
        diary.setImages(diaryImages);

        Diary savedDiary = diaryRepository.save(diary);
        return convertToDiaryResponse(savedDiary);
    }

    /**
     * Tạo bài viết nhật ký du ký mới bằng JSON payload (Presigned URL flow hỗ trợ nhiều ảnh)
     */
    @Transactional
    public DiaryResponse createDiaryJson(DiaryRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        // Kiểm tra ràng buộc: mỗi địa điểm trong một lịch trình chỉ được đăng 1 lần
        if (request.getSpotId() != null && request.getItineraryId() != null) {
            boolean alreadyPosted = diaryRepository.existsByUserIdAndSpotIdAndItineraryId(
                    request.getUserId(), request.getSpotId(), request.getItineraryId());
            if (alreadyPosted) {
                throw new RuntimeException("Địa điểm này đã được đăng trong lịch trình này. Hãy hoàn thành một lịch trình khác để đăng lại!");
            }
        }

        Diary diary = Diary.builder()
                .user(user)
                .category(request.getCategory())
                .contentVi(request.getContentVi())
                .contentEn(request.getContentEn())
                .spotId(request.getSpotId())
                .itineraryId(request.getItineraryId())
                .build();

        List<DiaryImage> diaryImages = new ArrayList<>();
        if (request.getImages() != null) {
            for (DiaryRequest.ImageDto imgDto : request.getImages()) {
                diaryImages.add(DiaryImage.builder()
                        .diary(diary)
                        .imageCfId(imgDto.getImageCfId())
                        .imageUrl(imgDto.getImageUrl())
                        .build());
            }
        }
        diary.setImages(diaryImages);

        Diary savedDiary = diaryRepository.save(diary);
        return convertToDiaryResponse(savedDiary);
    }

    /**
     * Lấy danh sách spotId đã được đăng bởi user trong một itinerary cụ thể
     */
    @Transactional(readOnly = true)
    public List<Long> getPostedSpotIds(Long userId, Long itineraryId) {
        return diaryRepository.findByUserIdAndItineraryId(userId, itineraryId)
                .stream()
                .filter(d -> d.getSpotId() != null)
                .map(Diary::getSpotId)
                .collect(Collectors.toList());
    }

    /**
     * Tăng số lượng Thả tim cho bài viết (Like)
     */
    @Transactional
    public void toggleLike(Long diaryId, Long userId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại!"));
        
        if (diary.getUser() != null && diary.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không được phép tự thích bài viết của chính mình!");
        }
        
        diary.setLikesCount(diary.getLikesCount() + 1);
        diaryRepository.save(diary);
    }

    /**
     * Đăng bình luận cho bài viết nhật ký du lịch (hỗ trợ bình luận con lồng nhau)
     */
    @Transactional
    public CommentResponse addComment(Long diaryId, Long userId, String content, Long parentCommentId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Comment parent = null;
        if (parentCommentId != null) {
            parent = commentRepository.findById(parentCommentId).orElse(null);
        }

        Comment comment = Comment.builder()
                .diary(diary)
                .user(user)
                .parentComment(parent)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .parentCommentId(parent != null ? parent.getId() : null)
                .createdAt(savedComment.getCreatedAt())
                .user(CommentResponse.AuthorInfo.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .avatarUrl(user.getAvatarUrl())
                        .build())
                .build();
    }

    /**
     * Converter nội bộ chuyển Diary sang DiaryResponse DTO
     */
    private DiaryResponse convertToDiaryResponse(Diary diary) {
        // Tải danh sách các bình luận của bài viết này
        List<Comment> comments = commentRepository.findByDiaryIdOrderByCreatedAtAsc(diary.getId());
        
        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> CommentResponse.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .parentCommentId(c.getParentComment() != null ? c.getParentComment().getId() : null)
                        .createdAt(c.getCreatedAt())
                        .user(CommentResponse.AuthorInfo.builder()
                                .id(c.getUser().getId())
                                .fullName(c.getUser().getFullName())
                                .email(c.getUser().getEmail())
                                .avatarUrl(c.getUser().getAvatarUrl())
                                .build())
                        .build())
                .collect(Collectors.toList());

        // Lấy thông tin legacy single image từ ảnh đầu tiên (nếu có) để tương thích ngược với Frontend cũ
        String legacyImageUrl = null;
        String legacyImageCfId = null;
        if (diary.getImages() != null && !diary.getImages().isEmpty()) {
            legacyImageUrl = diary.getImages().get(0).getImageUrl();
            legacyImageCfId = diary.getImages().get(0).getImageCfId();
        } else {
            legacyImageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=600&q=80";
        }

        vn.travelist.model.Spot spot = null;
        if (diary.getSpotId() != null) {
            try {
                spot = spotService.getSpotById(diary.getSpotId());
            } catch (Exception e) {
                // ignore if spot cannot be resolved
            }
        }

        DiaryResponse.DiaryResponseBuilder builder = DiaryResponse.builder()
                .id(diary.getId())
                .category(diary.getCategory())
                .contentVi(diary.getContentVi())
                .contentEn(diary.getContentEn())
                .imageCfId(legacyImageCfId)
                .imageUrl(legacyImageUrl)
                .images(diary.getImages())
                .likesCount(diary.getLikesCount())
                .createdAt(diary.getCreatedAt())
                .user(DiaryResponse.AuthorInfo.builder()
                        .id(diary.getUser().getId())
                        .fullName(diary.getUser().getFullName())
                        .email(diary.getUser().getEmail())
                        .role(diary.getUser().getRole())
                        .avatarUrl(diary.getUser().getAvatarUrl())
                        .build())
                .comments(commentResponses)
                .spotId(diary.getSpotId())
                .spot(spot);

        return builder.build();
    }
}
