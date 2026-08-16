package com.gamehub.service;

import com.gamehub.dto.AuthResponse;
import com.gamehub.dto.LoginRequest;
import com.gamehub.dto.RegisterRequest;
import com.gamehub.dto.UserDTO;
import com.gamehub.entity.User;
import com.gamehub.exception.ConflictException;
import com.gamehub.exception.NotFoundException;
import com.gamehub.exception.UnauthorizedException;
import com.gamehub.repository.UserRepository;
import com.gamehub.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);

        return new AuthResponse(jwtUtil.generateToken(user.getId()), UserDTO.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = findByEmailOrUsername(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return new AuthResponse(jwtUtil.generateToken(user.getId()), UserDTO.from(user));
    }

    private Optional<User> findByEmailOrUsername(String value) {
        return userRepository.findByEmail(value.trim().toLowerCase())
                .or(() -> userRepository.findByUsername(value.trim()));
    }
}
