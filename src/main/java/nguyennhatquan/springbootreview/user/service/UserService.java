package nguyennhatquan.springbootreview.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nguyennhatquan.springbootreview.user.dto.auth.CreateUserRequest;
import nguyennhatquan.springbootreview.user.dto.auth.LoginRequest;
import nguyennhatquan.springbootreview.user.dto.auth.LoginResponse;
import nguyennhatquan.springbootreview.user.entity.Role;
import nguyennhatquan.springbootreview.order.entity.Cart;
import nguyennhatquan.springbootreview.user.entity.User;
import nguyennhatquan.springbootreview.shared.exception.ResourceNotFoundException;
import nguyennhatquan.springbootreview.shared.exception.UnauthorizedException;
import nguyennhatquan.springbootreview.user.repository.UserRepository;
import nguyennhatquan.springbootreview.user.repository.RoleRepository;
import nguyennhatquan.springbootreview.order.repository.CartRepository;
import nguyennhatquan.springbootreview.shared.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CartRepository cartRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String token = tokenProvider.generateToken(authentication);
            User user = findByEmail(request.getEmail());

            log.info("User logged in successfully: {}", request.getEmail());

            return LoginResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().getName())
                    .accessToken(token)
                    .tokenType("Bearer")
                    .build();
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Transactional
    public LoginResponse register(CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new RuntimeException("Email already registered");
        }

        // Get default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> {
                    log.error("USER role not found in database");
                    return new ResourceNotFoundException("USER role not found");
                });

        // Create new user
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(userRole)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);

        // Create cart for user
        Cart cart = Cart.builder()
                .user(savedUser)
                .build();
        cartRepository.save(cart);

        log.info("User registered successfully: {}", savedUser.getEmail());

        // Generate token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = tokenProvider.generateToken(authentication);

        return LoginResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().getName())
                .accessToken(token)
                .tokenType("Bearer")
                .build();
    }

    public LoginResponse getUserProfile(String accessToken) {
        // Validate token and extract user information
        if (!tokenProvider.validateToken(accessToken)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        String email = tokenProvider.getEmailFromToken(accessToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().getName())
                .accessToken(null)
                .tokenType(null)
                .build();
    }
}
