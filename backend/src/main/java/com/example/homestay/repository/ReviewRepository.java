package com.example.homestay.repository;

import com.example.homestay.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByHomestayIdAndStatus(Long homestayId, String status);

    List<Review> findByUserId(Long userId);

    @Query("select r from Review r, Homestay h where r.homestayId = h.id and h.merchantId = :merchantId and r.status = 'NORMAL'")
    List<Review> findByMerchantId(@Param("merchantId") Long merchantId);
}
