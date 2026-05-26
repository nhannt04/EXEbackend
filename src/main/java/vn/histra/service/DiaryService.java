package vn.histra.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.histra.dto.CommentResponse;
import vn.histra.dto.DiaryResponse;
import vn.histra.model.Comment;
import vn.histra.model.Diary;
import vn.histra.model.Spot;
import vn.histra.model.User;
import vn.histra.repository.CommentRepository;
import vn.histra.repository.DiaryRepository;
import vn.histra.repository.SpotRepository;
import vn.histra.repository.UserRepository;
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
    private final SpotRepository spotRepository;
    private final CloudflareImageService cloudflareImageService;

    /**
     * Lấy toàn bộ nhật ký (có thể lọc theo chuyên mục) kèm tác giả, địa điểm và danh sách bình luận
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
     * Tạo bài viết nhật ký du ký mới kèm tệp ảnh upload lên Cloudflare
     */
    @Transactional
    public DiaryResponse createDiary(Long userId, String category, String contentVi, String contentEn, Long spotId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));

        Spot spot = null;
        if (spotId != null) {
            spot = spotRepository.findById(spotId).orElse(null);
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
                .imageCfId(imageCfId)
                .imageUrl(imageUrl)
                .build();
        
        if (spot != null) {
            diary.setSpot(spot);
        }

        Diary savedDiary = diaryRepository.save(diary);
        return convertToDiaryResponse(savedDiary);
    }

    /**
     * Tăng số lượng Thả tim cho bài viết (Like)
     */
    @Transactional
    public void toggleLike(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại!"));
        
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

        return DiaryResponse.builder()
                .id(diary.getId())
                .category(diary.getCategory())
                .contentVi(diary.getContentVi())
                .contentEn(diary.getContentEn())
                .imageCfId(diary.getImageCfId())
                .imageUrl(diary.getImageUrl())
                .likesCount(diary.getLikesCount())
                .createdAt(diary.getCreatedAt())
                .spot(diary.getSpot())
                .user(DiaryResponse.AuthorInfo.builder()
                        .id(diary.getUser().getId())
                        .fullName(diary.getUser().getFullName())
                        .email(diary.getUser().getEmail())
                        .role(diary.getUser().getRole())
                        .avatarUrl(diary.getUser().getAvatarUrl())
                        .build())
                .comments(commentResponses)
                .build();
    }
}
