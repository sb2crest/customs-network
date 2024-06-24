 package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.UserProductInfo;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.repository.UserProductInfoRepository;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.customs.network.fdapn.exception.ErrorResCodes.*;
import static com.customs.network.fdapn.utils.JsonUtils.convertJsonToValidationErrorList;
import static com.customs.network.fdapn.utils.ObjectValidations.validateCustomerProductInfoDto;

@Slf4j
@Service
public class UserProductInfoServicesImpl implements UserProductInfoServices {
    private final UserProductInfoRepository userProductInfoRepository;
    @Autowired
    public UserProductInfoServicesImpl(UserProductInfoRepository userProductInfoRepository) {
        this.userProductInfoRepository = userProductInfoRepository;
    }

    Cache<String, UserProductInfoDto> productInfoCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Override
    public String saveProduct(UserProductInfoDto userProductInfo) {
        validateCustomerProductInfoDto(userProductInfo);
        if (userProductInfoRepository.existsByUniqueUserIdentifierAndProductCode(userProductInfo.getUniqueUserIdentifier(), userProductInfo.getProductCode())) {
            log.error("product with code {} already exists", userProductInfo.getProductCode());
            return "product with code {} already exists" + userProductInfo.getProductCode();
        }
        try {
            UserProductInfo save = userProductInfoRepository.save(getUserProductInfo(userProductInfo));
            String cacheKey = userProductInfo.getUniqueUserIdentifier() + "_" + userProductInfo.getProductCode();
            cacheProductInfo(cacheKey, getUserProductInfoDto(save));
            return "Save product with code " + userProductInfo.getProductCode();
        } catch (DataAccessException e) {
            log.error("fail to save product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            return "fail to save product with code " +
                    userProductInfo.getProductCode();
        } catch (Exception e) {
            log.error("Unexpected error while saving product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            return "error while saving the product " +
                    userProductInfo.getProductCode();
        }
    }

    public void cacheProductInfo(String cacheKey, UserProductInfoDto productInfo) {
        productInfoCache.put(cacheKey, productInfo);
    }

    @Override
    @Cacheable(value = "productInfoCache", key = "#uniqueUserIdentifier + '_' + #productCode")
    public UserProductInfoDto getProductByProductCode(String uniqueUserIdentifier, String productCode) {
        log.info("Fetching from database");
        UserProductInfo userProductInfo = supplyUserProductInfo(uniqueUserIdentifier, productCode);
        UserProductInfoDto userProductInfoDto=getUserProductInfoDto(userProductInfo);
        String cacheKey = userProductInfo.getUniqueUserIdentifier() + "_" + userProductInfo.getProductCode();
        cacheProductInfo(cacheKey, userProductInfoDto);
        return userProductInfoDto;
    }

    @Override
    public List<String> getProductCodeList(String uniqueUserIdentifier) {
        return userProductInfoRepository.findProductCodeByUniqueUserIdentifier(uniqueUserIdentifier);
    }

    @Override
    @CacheEvict(value = "productInfoCache", key = "#uniqueUserIdentifier + '_' + #productCode")
    public String deleteProduct(String uniqueUserIdentifier, String productCode) {
        UserProductInfo userProductInfo = supplyUserProductInfo(uniqueUserIdentifier, productCode);
        try {
            userProductInfoRepository.delete(userProductInfo);
            return "Deleted product with code " + productCode;
        } catch (DataAccessException e) {
            log.error("Failed to delete product with code {} for the user {} due to database access error: {}", productCode, uniqueUserIdentifier, e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR, "Failed to delete product with code " + productCode);
        } catch (Exception e) {
            log.error("Unexpected error while deleting product with code {} for the user {}: {}", productCode, uniqueUserIdentifier, e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR, "error while deleting product with code "
                    + productCode);
        }
    }

    @Override
    public String updateProductInfo(UserProductInfoDto productInfoDto) {
        UserProductInfo userProductInfo = supplyUserProductInfo(productInfoDto.getUniqueUserIdentifier(),
                productInfoDto.getProductCode());
        try {
            userProductInfo.setProductInfo(productInfoDto.getProductInfo());
            userProductInfo.setValidationErrors(productInfoDto.getValidationErrors());
            userProductInfo.setValid(isValid(productInfoDto.getValidationErrors()));
            UserProductInfo save = userProductInfoRepository.save(userProductInfo);
            String cacheKey = userProductInfo.getUniqueUserIdentifier() + "_" + userProductInfo.getProductCode();
            cacheProductInfo(cacheKey, getUserProductInfoDto(save));
            return "Product updated successfully";
        } catch (DataAccessException e) {
            log.error("fail to update product with code {} for the user {} ,{} ",
                    productInfoDto.getProductCode(), productInfoDto.getUniqueUserIdentifier(), e.getMessage());
            return "Failed to update product with code " + productInfoDto.getProductCode();
        } catch (Exception e) {
            log.error("Unexpected error while updating product with code {} for the user {} ,{} ",
                    productInfoDto.getProductCode(), productInfoDto.getUniqueUserIdentifier(), e.getMessage());
            return "Error while updating product with code "
                    + productInfoDto.getProductCode();
        }
    }

    private UserProductInfo supplyUserProductInfo(String uniqueUserIdentifier, String productCode) {
        return userProductInfoRepository.findByUniqueUserIdentifierAndProductCode(uniqueUserIdentifier, productCode)
                .orElseThrow(() -> new FdapnCustomExceptions(RECORD_NOT_FOUND,
                        "No data found with product code " + productCode + " for user " + uniqueUserIdentifier));
    }
    @Override
    public List<UserProductInfoDto> fetchAllProducts(List<String> productCodes, String uniqueUserIdentifier) {
       return productCodes.parallelStream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    String cacheKey = uniqueUserIdentifier + "_" + obj;
                    UserProductInfoDto userProductInfoDto = productInfoCache.getIfPresent(cacheKey);
                    if (userProductInfoDto == null) {
                        userProductInfoDto = getProductByProductCode(uniqueUserIdentifier, obj);
                    }
                  return userProductInfoDto;
                }).toList();
    }
    @Override
    public List<ValidationError> getProductValidationErrors(List<String> productCodes, String uniqueUserIdentifier){
        List<ValidationError> validationErrors = new ArrayList<>();
        if (uniqueUserIdentifier == null) {
            return validationErrors;
        }
        productCodes.parallelStream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    String cacheKey = uniqueUserIdentifier + "_" + obj;
                    UserProductInfoDto userProductInfoDto = productInfoCache.getIfPresent(cacheKey);
                    if (userProductInfoDto == null) {
                        try {
                            userProductInfoDto = getProductByProductCode(uniqueUserIdentifier, obj);
                        } catch (FdapnCustomExceptions e) {
                            log.error("Failed to fetch product with code {} for the user {} due to database access error: {}", obj, uniqueUserIdentifier, e.getMessage());
                            ValidationError validationError = new ValidationError();
                            validationError.setFieldName("productCode");
                            validationError.setMessage("Product not found");
                            validationError.setActual(e.getMessage());
                            validationErrors.add(validationError);
                        }
                    }
                    if (userProductInfoDto != null && userProductInfoDto.getValidationErrors() != null) {
                            List<ValidationError> convertedErrors = convertJsonToValidationErrorList(userProductInfoDto.getValidationErrors());
                            validationErrors.addAll(convertedErrors);
                    }
                });
        return validationErrors;
    }

    public static UserProductInfo getUserProductInfo(UserProductInfoDto dto) {
        return UserProductInfo.builder()
                .productCode(dto.getProductCode())
                .uniqueUserIdentifier(dto.getUniqueUserIdentifier())
                .productInfo(dto.getProductInfo())
                .isValid(isValid(dto.getValidationErrors()))
                .validationErrors(dto.getValidationErrors())
                .build();
    }

    public static UserProductInfoDto getUserProductInfoDto(UserProductInfo entity) {
        return UserProductInfoDto.builder()
                .productCode(entity.getProductCode())
                .uniqueUserIdentifier(entity.getUniqueUserIdentifier())
                .productInfo(entity.getProductInfo())
                .validationErrors(entity.getValidationErrors())
                .isValid(isValid(entity.getValidationErrors()))
                .build();
    }

    private static boolean isValid(JsonNode jsonNode) {
        return jsonNode == null;
    }
}
