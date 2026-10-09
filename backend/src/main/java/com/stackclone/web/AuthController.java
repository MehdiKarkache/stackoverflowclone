package com.stackclone.web;

import com.stackclone.dto.AuthDtos.AuthResponse;
import com.stackclone.dto.AuthDtos.LoginRequest;
import com.stackclone.dto.AuthDtos.RegisterRequest;
import com.stackclone.dto.UserDtos.UserSummary;
import com.stackclone.repository.UserRepository;
import com.stackclone.security.CurrentUser;
import com.stackclone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserSummary me() {
        return users.findById(CurrentUser.requireId())
                .map(UserSummary::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }
}
