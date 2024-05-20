package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.UserProductInfo;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.repository.UserProductInfoRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserProductInfoServicesImpl implements UserProductInfoServices {
    private final UserProductInfoRepository userProductInfoRepository;

    public UserProductInfoServicesImpl(UserProductInfoRepository userProductInfoRepository) {
        this.userProductInfoRepository = userProductInfoRepository;
    }

    @Override
    public String save(UserProductInfoDto userProductInfo) {
        validateCustomerProductInfoDto(userProductInfo);
        try {
            userProductInfoRepository.save(getUserProductInfo(userProductInfo));
            return "Save product with code " + userProductInfo.getProductCode();
        } catch (DataAccessException e) {
            log.error("fail to save product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.FAIL_TO_SAVE_DATA,
                    "Failed to save the product " + userProductInfo.getProductCode());
        } catch (Exception e) {
            log.error("Unexpected error while saving product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.UNEXPECTED_ERROR,
                    "Unexpected error while saving the product " + userProductInfo.getProductCode());
        }
    }

    @Override
    public UserProductInfoDto getProductByProductCode(String productCode) {
        UserProductInfo userProductInfoOptional = userProductInfoRepository.findById(productCode)
                .orElseThrow(() -> new FdapnCustomExceptions(ErrorResCodes.RECORD_NOT_FOUND, "No data found with product code " + productCode));
        return getUserProductInfoDto(userProductInfoOptional);
    }

    private void validateCustomerProductInfoDto(UserProductInfoDto dto) {
        List<ValidationError> errorList = new ArrayList<>();
        if (StringUtils.isBlank(dto.getProductCode())) {
            ValidationError validationError = new ValidationError("productCode",
                    "Product code cannot be empty or null", dto.getProductCode());
            errorList.add(validationError);
        }
        if (StringUtils.isBlank(dto.getUserId())) {
            ValidationError validationError = new ValidationError("userId",
                    "userId cannot be empty or null", dto.getUserId());
            errorList.add(validationError);
        }
        if (dto.getProductInfo().isEmpty()) {
            ValidationError validationError = new ValidationError("productInfo",
                    "productInfo cannot be null", null);
            errorList.add(validationError);
        }
        if (!errorList.isEmpty()) {
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, errorList);
        }
    }

    private UserProductInfo getUserProductInfo(UserProductInfoDto dto) {
        return UserProductInfo.builder()
                .productCode(dto.getProductCode())
                .userId(dto.getUserId())
                .productInfo(dto.getProductInfo())
                .build();
    }

    private UserProductInfoDto getUserProductInfoDto(UserProductInfo entity) {
        return UserProductInfoDto.builder()
                .productCode(entity.getProductCode())
                .userId(entity.getUserId())
                .productInfo(entity.getProductInfo())
                .build();
    }
}
