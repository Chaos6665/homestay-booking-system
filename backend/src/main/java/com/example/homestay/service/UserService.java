package com.example.homestay.service;

import com.example.homestay.dto.UserLoginRequest;
import com.example.homestay.dto.UserLoginResponse;
import com.example.homestay.dto.UserRegisterRequest;

public interface UserService {

    void register(UserRegisterRequest request);

    UserLoginResponse login(UserLoginRequest request);
}
