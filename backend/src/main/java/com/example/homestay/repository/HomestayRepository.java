package com.example.homestay.repository;

import com.example.homestay.entity.Homestay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomestayRepository extends JpaRepository<Homestay, Long> {

    List<Homestay> findByCity(String city);

    List<Homestay> findByMerchantId(Long merchantId);
}
