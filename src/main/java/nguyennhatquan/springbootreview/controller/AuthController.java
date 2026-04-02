package nguyennhatquan.springbootreview.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.dto.ApiResponse;
import nguyennhatquan.springbootreview.dto.CreateUserRequest;
import nguyennhatquan.springbootreview.dto.LoginRequest;
import nguyennhatquan.springbootreview.dto.LoginResponse;
import nguyennhatquan.springbootreview.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());

        LoginResponse loginResponse = userService.login(request);

        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("Login successful")
                .data(loginResponse)
                .timestamp(LocalDateTime.now())
                .path("/api/auth/login")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Register request for email: {}", request.getEmail());

        LoginResponse registerResponse = userService.register(request);

        ApiResponse<LoginResponse> response = ApiResponse.<LoginResponse>builder()
                .code(201)
                .message("Registration successful")
                .data(registerResponse)
                .timestamp(LocalDateTime.now())
                .path("/api/auth/register")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
