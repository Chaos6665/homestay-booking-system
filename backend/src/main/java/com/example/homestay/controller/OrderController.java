package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.dto.CreateOrderRequest;
import com.example.homestay.dto.OrderResponse;
import com.example.homestay.entity.BookingOrder;
import com.example.homestay.entity.Homestay;
import com.example.homestay.repository.BookingOrderRepository;
import com.example.homestay.repository.HomestayRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final BookingOrderRepository bookingOrderRepository;
    private final HomestayRepository homestayRepository;

    public OrderController(BookingOrderRepository bookingOrderRepository, HomestayRepository homestayRepository) {
        this.bookingOrderRepository = bookingOrderRepository;
        this.homestayRepository = homestayRepository;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        if (request.getUserId() == null || request.getHomestayId() == null) {
            throw new IllegalArgumentException("用户和民宿不能为空");
        }
        LocalDate checkIn = LocalDate.parse(request.getCheckInDate());
        LocalDate checkOut = LocalDate.parse(request.getCheckOutDate());
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("离店日期必须晚于入住日期");
        }

        Homestay homestay = homestayRepository.findById(request.getHomestayId())
                .orElseThrow(() -> new IllegalArgumentException("民宿不存在"));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal total = homestay.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        BookingOrder order = new BookingOrder();
        order.setUserId(request.getUserId());
        order.setHomestayId(request.getHomestayId());
        order.setCheckInDate(checkIn);
        order.setCheckOutDate(checkOut);
        order.setGuestCount(request.getGuestCount());
        order.setTotalAmount(total);
        order.setStatus("PENDING");

        BookingOrder saved = bookingOrderRepository.save(order);
        return ApiResponse.success(toResponse(saved));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> listByUser(@PathVariable Long userId) {
        List<BookingOrder> list = bookingOrderRepository.findByUserId(userId);
        List<OrderResponse> resp = list.stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(resp);
    }

    private OrderResponse toResponse(BookingOrder o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setUserId(o.getUserId());
        r.setHomestayId(o.getHomestayId());
        r.setCheckInDate(o.getCheckInDate().toString());
        r.setCheckOutDate(o.getCheckOutDate().toString());
        r.setGuestCount(o.getGuestCount());
        r.setTotalAmount(o.getTotalAmount());
        r.setStatus(o.getStatus());
        return r;
    }
}
