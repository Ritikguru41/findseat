package com.findseat.service;

import com.findseat.dto.LoginRequest;
import com.findseat.dto.OtpRequest;
import com.findseat.dto.RegisterRequest;
import com.findseat.dto.ResendOtpRequest;
import com.findseat.dto.UserResponse;
import com.findseat.entity.User;
import com.findseat.enums.Role;
import com.findseat.exception.BadRequestException;
import com.findseat.exception.InvalidCredentialsException;
import com.findseat.exception.ResourceNotFoundException;
import com.findseat.exception.UnverifiedEmailException;
import com.findseat.exception.UserAlreadyExistsException;
import com.findseat.repository.UserRepository;
import com.findseat.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public Map<String, Object> register(RegisterRequest request) {
        String name = request.getName().trim();
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new BadRequestException("All fields are required");
        }
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BadRequestException("Invalid email format");
        }
        if (password.length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        String otp = generateOtp();
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(2);
        String hashedPassword = passwordEncoder.encode(password);

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User user = existing.get();
            if (user.isVerified()) {
                throw new UserAlreadyExistsException("Email already registered");
            }
            user.setName(name);
            user.setPassword(hashedPassword);
            user.setOtp(otp);
            user.setOtpExpiry(otpExpiry);
            userRepository.save(user);
        } else {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.setRole(Role.USER);
            user.setVerified(false);
            user.setOtp(otp);
            user.setOtpExpiry(otpExpiry);
            userRepository.save(user);
        }

        sendOtpSafely(email, name, otp);
        return Map.of("message", "OTP sent successfully to your email.");
    }

    public Map<String, Object> verifyOtp(OtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!request.getOtp().trim().equals(user.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new BadRequestException("OTP expired, please request a new one");
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return Map.of("message", "Email verified successfully! You can now login.");
    }

    public Map<String, Object> resendOtp(ResendOtpRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isVerified()) {
            throw new BadRequestException("Email already verified");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(2));
        userRepository.save(user);

        sendOtpSafely(email, user.getName(), otp);
        return Map.of("message", "New OTP sent to your email");
    }

    public Map<String, Object> login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isVerified()) {
            throw new UnverifiedEmailException("Please verify your email");
        }

        String token = jwtUtil.generateToken(user);

        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole().name().toLowerCase());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("user", userMap);
        return response;
    }

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name().toLowerCase(),
                user.isVerified(),
                user.getCreatedAt());
    }

    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    private void sendOtpSafely(String email, String name, String otp) {
        try {
            emailService.sendOtpEmail(email, name, otp);
        } catch (Exception ex) {
            log.error("Email sending failed for {}: {}", email, ex.getMessage());
            log.info("[DEBUG] OTP for {} is: {}", email, otp);
        }
    }
}
