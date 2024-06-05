package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;

import java.util.List;

public interface UserProductInfoServices {
    String saveProduct(UserProductInfoDto customerProductInfo);

    void cacheProductInfo(String cacheKey, UserProductInfoDto productInfo);

    UserProductInfoDto getProductByProductCode(String uniqueUserIdentifier, String productCode);

    List<String> getProductCodeList(String uniqueUserIdentifier);

    String deleteProduct(String uniqueUserIdentifier, String productCode);

    String updateProductInfo(UserProductInfoDto productInfoDto);

}
