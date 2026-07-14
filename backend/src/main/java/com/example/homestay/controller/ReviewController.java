package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.dto.ReplyRequest;
import com.example.homestay.dto.ReviewRequest;
import com.example.homestay.dto.ReviewResponse;
import com.example.homestay.entity.Review;
import com.example.homestay.repository.ReviewRepository;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public ApiResponse<List<ReviewResponse>> listAll() {
        List<Review> list = reviewRepository.findAll();
        List<ReviewResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<ReviewResponse> create(@RequestBody ReviewRequest request) {
        if (request.getHomestayId() == null || request.getUserId() == null) {
            throw new IllegalArgumentException("民宿和用户不能为空");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("评分必须是1-5");
        }
        Review review = new Review();
        review.setHomestayId(request.getHomestayId());
        review.setUserId(request.getUserId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setStatus("NORMAL");
        Review saved = reviewRepository.save(review);
        return ApiResponse.success(toResponse(saved));
    }

    @GetMapping("/homestay/{homestayId}")
    public ApiResponse<List<ReviewResponse>> listByHomestay(@PathVariable Long homestayId) {
        List<Review> list = reviewRepository.findByHomestayIdAndStatus(homestayId, "NORMAL");
        List<ReviewResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<ReviewResponse>> listByMerchant(@PathVariable Long merchantId) {
        List<Review> list = reviewRepository.findByMerchantId(merchantId);
        List<ReviewResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<ReviewResponse> reply(@PathVariable Long id, @RequestBody ReplyRequest request) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        review.setMerchantReply(request.getMerchantReply());
        Review saved = reviewRepository.save(review);
        return ApiResponse.success(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<ReviewResponse> updateStatus(@PathVariable Long id, @RequestBody ReviewResponse req) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (req.getStatus() != null) {
            review.setStatus(req.getStatus());
        }
        if (req.getContent() != null) {
            review.setContent(req.getContent());
        }
        Review saved = reviewRepository.save(review);
        return ApiResponse.success(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        review.setStatus("HIDDEN");
        reviewRepository.save(review);
        return ApiResponse.success("删除成功", null);
    }

    private ReviewResponse toResponse(Review r) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(r.getId());
        resp.setHomestayId(r.getHomestayId());
        resp.setUserId(r.getUserId());
        resp.setRating(r.getRating());
        resp.setContent(r.getContent());
        resp.setMerchantReply(r.getMerchantReply());
        resp.setStatus(r.getStatus());
        if (r.getCreatedAt() != null) {
            resp.setCreatedAt(r.getCreatedAt().format(FORMATTER));
        }
        return resp;
    }
}
