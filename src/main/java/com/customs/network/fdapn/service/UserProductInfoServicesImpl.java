package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.UserProductInfo;
import com.customs.network.fdapn.repository.UserProductInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.List;
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

    @Override
    public String saveProduct(UserProductInfoDto userProductInfo) {
        validateCustomerProductInfoDto(userProductInfo);
        if (userProductInfoRepository.existsByUniqueUserIdentifierAndProductCode(userProductInfo.getUniqueUserIdentifier(), userProductInfo.getProductCode())) {
            log.error("product with code {} already exists", userProductInfo.getProductCode());
            throw new FdapnCustomExceptions(ALREADY_EXISTING,
                    "product with code " + userProductInfo.getProductCode() + " already exists. Try to update");
        }
        try {
            userProductInfoRepository.save(getUserProductInfo(userProductInfo));
            return "Save product with code " + userProductInfo.getProductCode();
        } catch (DataAccessException e) {
            log.error("fail to save product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "Failed to save the product " + userProductInfo.getProductCode());
        } catch (Exception e) {
            log.error("Unexpected error while saving product with code {} ,{} ",
                    userProductInfo.getProductCode(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "error while saving the product " + userProductInfo.getProductCode());
        }
    }

    @Override
    public UserProductInfoDto getProductByProductCode(String uniqueUserIdentifier, String productCode) {
        UserProductInfo userProductInfo = supplyUserProductInfo(uniqueUserIdentifier, productCode);
        return getUserProductInfoDto(userProductInfo);
    }

    @Override
    public List<String> getProductCodeList(String uniqueUserIdentifier) {
        return userProductInfoRepository.findProductCodeByUniqueUserIdentifier(uniqueUserIdentifier);
    }

    @Override
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
                    + productCode );
        }
    }

    @Override
    public String updateProductInfo(UserProductInfoDto productInfoDto) {
        validateCustomerProductInfoDto(productInfoDto);
        UserProductInfo userProductInfo = supplyUserProductInfo(productInfoDto.getUniqueUserIdentifier(),
                productInfoDto.getProductCode());
        try {
            userProductInfo.setProductInfo(productInfoDto.getProductInfo());
            userProductInfoRepository.save(userProductInfo);
            return "Product updated successfully";
        } catch (DataAccessException e) {
            log.error("fail to update product with code {} for the user {} ,{} ",
                    productInfoDto.getProductCode(), productInfoDto.getUniqueUserIdentifier(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "Failed to update product with code " + productInfoDto.getProductCode());
        } catch (Exception e) {
            log.error("Unexpected error while updating product with code {} for the user {} ,{} ",
                    productInfoDto.getProductCode(), productInfoDto.getUniqueUserIdentifier(), e.getMessage());
            throw new FdapnCustomExceptions(SERVER_ERROR,
                    "Error while updating product with code "
                            + productInfoDto.getProductCode());
        }
    }

    private UserProductInfo supplyUserProductInfo(String uniqueUserIdentifier, String productCode) {
        return userProductInfoRepository.findByUniqueUserIdentifierAndProductCode(uniqueUserIdentifier, productCode)
                .orElseThrow(() -> new FdapnCustomExceptions(RECORD_NOT_FOUND,
                        "No data found with product code " + productCode + " for user " + uniqueUserIdentifier));
    }

    public static UserProductInfo getUserProductInfo(UserProductInfoDto dto) {
        return UserProductInfo.builder()
                .productCode(dto.getProductCode())
                .uniqueUserIdentifier(dto.getUniqueUserIdentifier())
                .productInfo(dto.getProductInfo())
                .build();
    }

    public static UserProductInfoDto getUserProductInfoDto(UserProductInfo entity) {
        return UserProductInfoDto.builder()
                .productCode(entity.getProductCode())
                .uniqueUserIdentifier(entity.getUniqueUserIdentifier())
                .productInfo(entity.getProductInfo())
                .build();
    }
}
