package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.UserProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserProductInfoRepository extends JpaRepository<UserProductInfo,Long> {
    Optional<UserProductInfo> findByUniqueUserIdentifierAndProductCode(String uniqueUserIdentifier, String productCode);
    @Query(value = "SELECT product_code FROM user_product_info WHERE unique_user_identifier = ?1", nativeQuery = true)
    List<String> findProductCodeByUniqueUserIdentifier(String uniqueUserIdentifier);

    boolean existsByUniqueUserIdentifierAndProductCode(String uniqueUserIdentifier, String productCode);

}
