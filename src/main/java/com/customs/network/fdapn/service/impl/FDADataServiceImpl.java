package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.PartialCodeRequest;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.service.FDADataService;
import com.customs.network.fdapn.utils.ApiEndpointsProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.customs.network.fdapn.exception.ErrorResCodes.SERVER_ERROR;
import static com.customs.network.fdapn.exception.ErrorResCodes.SERVICE_UNAVAILABLE;

@Service
@Slf4j
public class FDADataServiceImpl implements FDADataService {
    private final ApiEndpointsProperties apiProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public FDADataServiceImpl(ApiEndpointsProperties apiProperties, RestTemplate restTemplate) {
        this.apiProperties = apiProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public JsonNode getAllIndustry() throws IOException {
        return executeRequest(buildUrl(apiProperties.getIndustryEndpoint()));
    }

    @Override
    public JsonNode getByIndustryId(Integer id) throws IOException {
        String url = apiProperties.getIndustryIdEndpoint().replace("{industryid}", String.valueOf(id));
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getSubClassData() throws IOException {
        return executeRequest(buildUrl(apiProperties.getSubClassEndpoint()));
    }

    @Override
    public JsonNode getBySubclassId(String id) throws IOException {
        String url = apiProperties.getSubClassIdEndpoint().replace("{subclassid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getByIndustrySubclassId(String id) throws IOException {
        String url = apiProperties.getIndustrySubClassId().replace("{industryid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode fetchClass() throws IOException {
        return executeRequest(buildUrl(apiProperties.getClassEndpoint()));
    }

    @Override
    public JsonNode getByClassId(String id) throws IOException {
        String url = apiProperties.getClassIdEndpoint().replace("{classid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getByIndustryClassId(String id) throws IOException {
        String url = apiProperties.getIndustryClassIdEndpoint().replace("{industryid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getPic() throws IOException {
        return executeRequest(buildUrl(apiProperties.getPicEndpoint()));
    }

    @Override
    public JsonNode getByPicId(String id) throws IOException {
        String url = apiProperties.getPicIdEndpoint().replace("{picid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getByIndustryPic(String id) throws IOException {
        String url = apiProperties.getIndustryPicEndpoint().replace("{industryid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getProduct() throws IOException {
        return executeRequest(buildUrl(apiProperties.getProductEndpoint()));
    }

    @Override
    public JsonNode getByProductId(String id) throws IOException {
        String url = apiProperties.getProductIdEndpoint().replace("{productId}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getByProductName(String name) throws IOException {
        String url = apiProperties.getProductNameEndpoint().replace("{name}", name);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getProductNameByFormData(String name) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("payload", name);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(formData, setupHttpEntity().getHeaders());
        String url = apiProperties.getProductNameFormDataEndpoint();
        return executeRequestForProduct(entity, buildUrl(url));
    }

    @Override
    public JsonNode getByIndustryProductByIndustryId(String id) throws IOException {
        String url = apiProperties.getIndustryProductEndpoint().replace("{industryid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode validateProductCode(String code) throws IOException {
        String url = apiProperties.getProductCodeEndpoint().replace("{code}", code);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getProductCodeByIndustry(String id) throws IOException {
        String url = apiProperties.getProductCodeByIndustryEndpoint().replace("{industryid}", id);
        return executeRequest(buildUrl(url));
    }

    @Override
    public JsonNode getCodes(PartialCodeRequest request) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiProperties.getBaseUrl() + apiProperties.getPartialCodeEndpoint());
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("industry", request.getIndustry());
        queryParams.put("class", request.getIndustryClass());
        queryParams.put("subclass",  request.getSubclass());
        queryParams.put("pic", request.getPic());
        queryParams.put("group", request.getGroup());

        Map<String, String> filteredParams = queryParams.entrySet().stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        filteredParams.forEach(builder::queryParam);

        URI uri = builder.build().toUri();
        log.info("url {}", uri);
        return executeRequest(String.valueOf(uri));
    }

    private JsonNode executeRequest(String url) throws IOException {
        try {
            HttpEntity<String> entity = setupHttpEntity();
            return restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class).getBody();
        } catch (Exception e) {
            log.error("Failed to fetch data. Exception: {}", e.getMessage());
            throw new IOException("Failed to fetch data due to network issue", e);
        }
    }

    private JsonNode executeRequestForProduct(HttpEntity<MultiValueMap<String, Object>> entity, String url) {
        try {
            return restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class).getBody();
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            throw new FdapnCustomExceptions(SERVICE_UNAVAILABLE, " 503 - Service Unavailable.");
        } catch (Exception e) {
            throw new FdapnCustomExceptions(SERVER_ERROR, "Failed to fetch product name");
        }
    }

    private HttpEntity<String> setupHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization-Key", apiProperties.getApiKey());
        headers.set("Authorization-User", apiProperties.getUserId());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private String buildUrl(String endpoint) {
        return apiProperties.getBaseUrl() + endpoint;
    }

}
