package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.dto.HomestayResponse;
import com.example.homestay.entity.Homestay;
import com.example.homestay.repository.HomestayRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchant/homestays")
public class MerchantHomestayController {

    private final HomestayRepository homestayRepository;

    public MerchantHomestayController(HomestayRepository homestayRepository) {
        this.homestayRepository = homestayRepository;
    }

    @GetMapping
    public ApiResponse<List<HomestayResponse>> list(@RequestParam Long merchantId) {
        List<Homestay> list = homestayRepository.findByMerchantId(merchantId).stream()
                .filter(h -> h.getStatus() == null || "NORMAL".equals(h.getStatus()))
                .collect(Collectors.toList());
        List<HomestayResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    @PostMapping
    public ApiResponse<HomestayResponse> create(@RequestParam Long merchantId,
                                                @RequestBody HomestayResponse req) {
        Homestay h = new Homestay();
        h.setMerchantId(merchantId);
        h.setTitle(req.getTitle());
        h.setDescription(req.getDescription());
        h.setCity(req.getCity());
        h.setAddress(req.getAddress());
        h.setMaxGuests(req.getMaxGuests());
        h.setPricePerNight(req.getPricePerNight() == null ? BigDecimal.ZERO : req.getPricePerNight());
        h.setCoverImageUrl(req.getCoverImageUrl());
        h.setStatus("NORMAL");
        Homestay saved = homestayRepository.save(h);
        return ApiResponse.success(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ApiResponse<HomestayResponse> update(@PathVariable Long id,
                                                @RequestParam Long merchantId,
                                                @RequestBody HomestayResponse req) {
        Homestay h = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("民宿不存在"));
        if (!merchantId.equals(h.getMerchantId())) {
            throw new IllegalArgumentException("无权操作该民宿");
        }
        if (req.getTitle() != null) h.setTitle(req.getTitle());
        if (req.getDescription() != null) h.setDescription(req.getDescription());
        if (req.getCity() != null) h.setCity(req.getCity());
        if (req.getAddress() != null) h.setAddress(req.getAddress());
        if (req.getMaxGuests() != null) h.setMaxGuests(req.getMaxGuests());
        if (req.getPricePerNight() != null) h.setPricePerNight(req.getPricePerNight());
        if (req.getCoverImageUrl() != null) h.setCoverImageUrl(req.getCoverImageUrl());
        Homestay saved = homestayRepository.save(h);
        return ApiResponse.success(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam Long merchantId) {
        Homestay h = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("民宿不存在"));
        if (!merchantId.equals(h.getMerchantId())) {
            throw new IllegalArgumentException("无权操作该民宿");
        }
        h.setStatus("DELETED");
        homestayRepository.save(h);
        return ApiResponse.success("删除成功", null);
    }

    private HomestayResponse toResponse(Homestay h) {
        HomestayResponse r = new HomestayResponse();
        r.setId(h.getId());
        r.setTitle(h.getTitle());
        r.setDescription(h.getDescription());
        r.setCity(h.getCity());
        r.setAddress(h.getAddress());
        r.setMaxGuests(h.getMaxGuests());
        r.setPricePerNight(h.getPricePerNight());
        r.setCoverImageUrl(h.getCoverImageUrl());
        return r;
    }
}
