package com.customs.network.fdapn.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class ApiEndpointsProperties {
    @Value("${api.key}")
    private String apiKey;

    @Value("${api.user}")
    private String userId;

    @Value("${api.base-url}")
    private String baseUrl;

    @Value("${api.industry-endpoint}")
    private String industryEndpoint;

    @Value("${api.industryId-endpoint}")
    private String industryIdEndpoint;

    @Value("${api.subclass-endpoint}")
    private String subClassEndpoint;

    @Value("${api.subclassId-endpoint}")
    private String subClassIdEndpoint;

    @Value("${api.industrySubclassId-endpoint}")
    private String industrySubClassId;

    @Value("${api.class-endpoint}")
    private String classEndpoint;

    @Value("${api.classId-endpoint}")
    private String classIdEndpoint;

    @Value("${api.industryClass-endpoint}")
    private String industryClassIdEndpoint;

    @Value("${api.pic-endpoint}")
    private String picEndpoint;

    @Value("${api.picId-endpoint}")
    private String picIdEndpoint;

    @Value("${api.industrypic-endpoint}")
    private String industryPicEndpoint;

    @Value("${api.product-endpoint}")
    private String productEndpoint;

    @Value("${api.productId-endpoint}")
    private String productIdEndpoint;

    @Value("${api.productName-endpoint}")
    private String productNameEndpoint;

    @Value("${api.productName-formdata-endpoint}")
    private String productNameFormDataEndpoint;

    @Value("${api.industryProduct-endpoint}")
    private String industryProductEndpoint;

    @Value("${api.productCode-endpoint}")
    private String productCodeEndpoint;

    @Value("${api.productCodeByIndustry-endpoint}")
    private String productCodeByIndustryEndpoint;

    @Value("${api.partialCode-endpoint}")
    private String partialCodeEndpoint;


}
