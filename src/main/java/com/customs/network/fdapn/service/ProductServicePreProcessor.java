package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;

import java.util.List;

public interface ProductServicePreProcessor {
    void processProductInfo(List<UserProductInfoDto> data);

    //Actions
    String saveAction(UserProductInfoDto object);

    String updateAction(UserProductInfoDto object);

    String deleteAction(UserProductInfoDto object);

    UserProductInfoDto editAction(UserProductInfoDto object);
}
