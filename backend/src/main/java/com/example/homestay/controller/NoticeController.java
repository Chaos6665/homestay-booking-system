package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.entity.Notice;
import com.example.homestay.repository.NoticeRepository;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeRepository noticeRepository;

    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @GetMapping
    public ApiResponse<List<Notice>> listAll() {
        List<Notice> list = noticeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(list);
    }

    @PostMapping
    public ApiResponse<Notice> create(@RequestBody Notice notice) {
        notice.setId(null);
        Notice saved = noticeRepository.save(notice);
        return ApiResponse.success(saved);
    }

    @PutMapping("/{id}")
    public ApiResponse<Notice> update(@PathVariable Long id, @RequestBody Notice req) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        if (req.getTitle() != null) {
            notice.setTitle(req.getTitle());
        }
        if (req.getContent() != null) {
            notice.setContent(req.getContent());
        }
        Notice saved = noticeRepository.save(notice);
        return ApiResponse.success(saved);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeRepository.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}
