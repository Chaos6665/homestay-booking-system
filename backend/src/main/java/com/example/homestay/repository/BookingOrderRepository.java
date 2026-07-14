package com.example.homestay.repository;

import com.example.homestay.entity.BookingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingOrderRepository extends JpaRepository<BookingOrder, Long> {

    List<BookingOrder> findByUserId(Long userId);

    List<BookingOrder> findByHomestayId(Long homestayId);
}
