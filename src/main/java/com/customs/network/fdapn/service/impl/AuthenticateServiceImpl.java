package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.AuthenticationRequest;
import com.customs.network.fdapn.model.AuthenticationResponse;
import com.customs.network.fdapn.model.RegisterRequest;
import com.customs.network.fdapn.service.AuthenticateService;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.POST;

@Service
@AllArgsConstructor
public class AuthenticateServiceImpl implements AuthenticateService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://localhost:8081";

    @Override
    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request) {
        String url = "/api/v1/auth/register";
        String registerUrl = BASE_URL + url;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> requestEntity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                registerUrl,
                POST,
                requestEntity,
                AuthenticationResponse.class
        );
    }

    @Override
    public ResponseEntity<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        String url = "/api/v1/auth/authenticate";
        String authenticateUrl = BASE_URL + url;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(
                authenticateUrl,
                HttpMethod.POST,
                requestEntity,
                AuthenticationResponse.class
        );
    }
}
