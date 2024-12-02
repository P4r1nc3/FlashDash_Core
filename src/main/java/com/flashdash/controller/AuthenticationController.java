package com.flashdash.controller;

import com.flashdash.dto.AuthenticationResponse;
import com.flashdash.dto.LoginRequest;
import com.flashdash.dto.RegisterRequest;
import com.flashdash.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }
}
