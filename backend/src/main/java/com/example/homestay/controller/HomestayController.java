package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.dto.HomestayResponse;
import com.example.homestay.entity.Homestay;
import com.example.homestay.repository.HomestayRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/homestays")
public class HomestayController {

    private final HomestayRepository homestayRepository;

    public HomestayController(HomestayRepository homestayRepository) {
        this.homestayRepository = homestayRepository;
    }

    @GetMapping
    public ApiResponse<List<HomestayResponse>> list(@RequestParam(value = "city", required = false) String city) {
        List<Homestay> list;
        if (city != null && !city.trim().isEmpty()) {
            list = homestayRepository.findByCity(city);
        } else {
            list = homestayRepository.findAll();
        }
        List<HomestayResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    @GetMapping("/{id}")
    public ApiResponse<HomestayResponse> detail(@PathVariable Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("民宿不存在"));
        return ApiResponse.success(toResponse(homestay));
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
