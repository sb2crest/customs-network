package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;

public interface UserProductInfoServices {
    String save(UserProductInfoDto customerProductInfo);

    UserProductInfoDto getProductByProductCode(String productCode);
}
