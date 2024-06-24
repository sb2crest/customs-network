package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;

import java.util.List;

public interface ProductServicePreProcessor {
    void processProductInfo(List<UserProductInfoDto> data);
}
