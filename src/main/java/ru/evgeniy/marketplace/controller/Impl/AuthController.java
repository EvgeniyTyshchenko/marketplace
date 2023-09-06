package ru.evgeniy.marketplace.controller.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.evgeniy.marketplace.controller.AuthApiController;
import ru.evgeniy.marketplace.dto.other.Credentials;
import ru.evgeniy.marketplace.service.AuthService;

@CrossOrigin(value = "http://localhost:3000")
@RestController
public class AuthController implements AuthApiController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Credentials credentials) {
        authService.login(credentials);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Credentials credentials) {
        authService.register(credentials);
        return ResponseEntity.ok().build();
    }
}