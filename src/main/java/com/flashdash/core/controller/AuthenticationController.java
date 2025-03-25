package com.flashdash.core.controller;

import com.flashdash.core.service.AuthenticationService;
import com.p4r1nc3.flashdash.core.model.AuthenticationResponse;
import com.p4r1nc3.flashdash.core.model.LoginRequest;
import com.p4r1nc3.flashdash.core.model.RegisterRequest;
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

    @PostMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        authenticationService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully!");
    }
}
