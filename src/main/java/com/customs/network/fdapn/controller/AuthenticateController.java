package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.AuthenticationRequest;
import com.customs.network.fdapn.model.AuthenticationResponse;
import com.customs.network.fdapn.model.RegisterRequest;
import com.customs.network.fdapn.service.AuthenticateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@CrossOrigin("http://localhost:5173")
public class AuthenticateController {
    private final AuthenticateService authenticateService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticateService.register(request).getBody());
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticateService.authenticate(request).getBody());
    }
}
