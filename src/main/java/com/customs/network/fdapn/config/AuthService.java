package com.customs.network.fdapn.config;

import com.amazonaws.services.alexaforbusiness.model.UnauthorizedException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    private final String apiGatewayBaseUrl;

    public AuthService(RestTemplate restTemplate, @Value("${api.gateway.base.url}") String apiGatewayBaseUrl) {
        this.restTemplate = restTemplate;
        this.apiGatewayBaseUrl = apiGatewayBaseUrl;
    }

    private void callApiWithAuthentication(String jwtToken) {
        if (StringUtils.isBlank(jwtToken)) {
            throw new UnauthorizedException("JWT token is missing in the request headers.");
        }

        String urlWithToken = apiGatewayBaseUrl + "?token=" + jwtToken;
        try {
            restTemplate.exchange(urlWithToken, HttpMethod.GET, null, String.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new UnauthorizedException("Unauthorized access to the resource.");
        }
    }
    public void callApiWithAuthentication(HttpServletRequest request) {
        String jwtToken = request.getHeader("Authorization");
        callApiWithAuthentication(jwtToken);
    }
}
