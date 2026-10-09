package com.stackclone.service;

import com.stackclone.dto.AuthDtos.AuthResponse;
import com.stackclone.dto.AuthDtos.LoginRequest;
import com.stackclone.dto.AuthDtos.RegisterRequest;
import com.stackclone.dto.UserDtos.UserSummary;
import com.stackclone.model.User;
import com.stackclone.repository.UserRepository;
import com.stackclone.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (users.existsByUsernameIgnoreCase(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce nom d'utilisateur est déjà pris");
        }
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
        }
        User user = users.save(new User(req.username(), req.email().toLowerCase(),
                passwordEncoder.encode(req.password())));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String login = req.login().trim();
        User user = (login.contains("@") ? users.findByEmailIgnoreCase(login) : users.findByUsernameIgnoreCase(login))
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(jwtService.issueToken(user), UserSummary.from(user));
    }
}
