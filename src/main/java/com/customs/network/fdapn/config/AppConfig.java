package com.customs.network.fdapn.config;

import com.converter.service.ConverterService;
import com.converter.service.JsonToEdi;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    private static final String ORGANIZATION = "FDA";

    @Bean
    public RestTemplate restTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setMaxRedirects(50)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    @Bean
    public ConverterService getConverterService() {
        //Configuring edi mapping with organization
        JsonToEdi jsonToEdi = new JsonToEdi(ORGANIZATION);
        return new ConverterService(jsonToEdi);
    }
}
