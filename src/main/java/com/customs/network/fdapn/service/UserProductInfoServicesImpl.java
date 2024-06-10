package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.UserProductInfo;
import com.customs.network.fdapn.repository.UserProductInfoRepository;
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
        String cacheKey = userProductInfo.getUniqueUserIdentifier() + "_" + userProductInfo.getProductCode();
        cacheProductInfo(cacheKey, userProductInfo);
        if (userProductInfoRepository.existsByUniqueUserIdentifierAndProductCode(userProductInfo.getUniqueUserIdentifier(), userProductInfo.getProductCode())) {
            log.error("product with code {} already exists", userProductInfo.getProductCode());
            return "product with code {} already exists" + userProductInfo.getProductCode();
        }
        try {
            userProductInfoRepository.save(getUserProductInfo(userProductInfo));
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

    @Override
    public void cacheProductInfo(String cacheKey, UserProductInfoDto productInfo) {
        productInfoCache.put(cacheKey, productInfo);
    }

    @Override
    @Cacheable(value = "productInfoCache", key = "#uniqueUserIdentifier + '_' + #productCode")
    public UserProductInfoDto getProductByProductCode(String uniqueUserIdentifier, String productCode) {
        log.info("Fetching from database");
        UserProductInfo userProductInfo = supplyUserProductInfo(uniqueUserIdentifier, productCode);
        return getUserProductInfoDto(userProductInfo);
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
        String cacheKey = productInfoDto.getUniqueUserIdentifier() + "_" + productInfoDto.getProductCode();
        cacheProductInfo(cacheKey, productInfoDto);
        UserProductInfo userProductInfo = supplyUserProductInfo(productInfoDto.getUniqueUserIdentifier(),
                productInfoDto.getProductCode());
        try {
            userProductInfo.setProductInfo(productInfoDto.getProductInfo());
            userProductInfo.setValidationErrors(productInfoDto.getValidationErrors());
            userProductInfo.setValid(isValid(productInfoDto.getValidationErrors()));
            userProductInfoRepository.save(userProductInfo);
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
    public Map<Boolean, List<UserProductInfoDto>> fetchAllProducts(List<String> productCodes, String uniqueUserIdentifier) {
        Map<Boolean, List<UserProductInfoDto>> allProducts = new HashMap<>();
        List<UserProductInfoDto> validProducts = new ArrayList<>();
        List<UserProductInfoDto> invalidProducts = new ArrayList<>();
        productCodes.stream()
                .filter(Objects::nonNull)
                .forEach(obj -> {
                    UserProductInfoDto userProductInfoDto = getProductByProductCode(uniqueUserIdentifier, obj);
                    if (userProductInfoDto.isValid()) {
                        validProducts.add(userProductInfoDto);
                    } else {
                        invalidProducts.add(userProductInfoDto);
                    }
                });
        if (!invalidProducts.isEmpty()) {
            allProducts.put(false, invalidProducts);
            return allProducts;
        }
        allProducts.put(true, validProducts);
        return allProducts;
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
