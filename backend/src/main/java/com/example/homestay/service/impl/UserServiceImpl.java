package com.example.homestay.service.impl;

import com.example.homestay.dto.UserLoginRequest;
import com.example.homestay.dto.UserLoginResponse;
import com.example.homestay.dto.UserRegisterRequest;
import com.example.homestay.entity.User;
import com.example.homestay.repository.UserRepository;
import com.example.homestay.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void register(UserRegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new IllegalArgumentException("用户名已存在");
        });

        if (StringUtils.hasText(request.getPhone())) {
            userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
                throw new IllegalArgumentException("手机号已被注册");
            });
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 简化实现，后续可改为加密
        user.setPhone(request.getPhone());
        user.setNickname(request.getNickname());
        String role = request.getRole();
        if (!StringUtils.hasText(role)) {
            role = "USER";
        }
        user.setRole(role.toUpperCase());

        userRepository.save(user);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        UserLoginResponse resp = new UserLoginResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setPhone(user.getPhone());
        resp.setNickname(user.getNickname());
        resp.setRole(user.getRole());
        return resp;
    }
}
