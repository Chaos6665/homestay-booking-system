package com.example.homestay.controller;

import com.example.homestay.dto.ApiResponse;
import com.example.homestay.entity.User;
import com.example.homestay.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<User>> list(@RequestParam(value = "role", required = false) String role) {
        List<User> all = userRepository.findAll();
        if (role != null && !role.trim().isEmpty()) {
            String target = role.trim().toUpperCase();
            all = all.stream().filter(u -> target.equalsIgnoreCase(u.getRole())).collect(Collectors.toList());
        }
        return ApiResponse.success(all);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Long id, @RequestBody User req) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }
        if (req.getEnabled() != null) {
            user.setEnabled(req.getEnabled());
        }
        User saved = userRepository.save(user);
        return ApiResponse.success(saved);
    }
}
