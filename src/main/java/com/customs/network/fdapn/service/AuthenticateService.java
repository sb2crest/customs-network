package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.AuthenticationRequest;
import com.customs.network.fdapn.model.AuthenticationResponse;
import com.customs.network.fdapn.model.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthenticateService {
    ResponseEntity<AuthenticationResponse> register(RegisterRequest request);
    ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request);
}
