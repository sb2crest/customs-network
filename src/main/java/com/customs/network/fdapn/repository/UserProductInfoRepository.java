package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.UserProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserProductInfoRepository extends JpaRepository<UserProductInfo,Long> {
    Optional<UserProductInfo> findByUniqueUserIdentifierAndProductCode(String uniqueUserIdentifier, String productCode);

    @Query(value = "SELECT u.product_info FROM user_product_info u WHERE u.unique_user_identifier = :uniqueUserIdentifier AND u.product_code IN (:productCodeList) ORDER BY u.id LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Object[]> findProductInfoByUniqueUserIdentifierAndProductCodeList(
            @Param("uniqueUserIdentifier") String uniqueUserIdentifier,
            @Param("productCodeList") List<String> productCodeList,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = "SELECT COUNT(u) FROM UserProductInfo u WHERE u.uniqueUserIdentifier = :uniqueUserIdentifier AND u.productCode IN :productCodeList")
    Long countProductInfoByUniqueUserIdentifierAndProductCodeList(
            @Param("uniqueUserIdentifier") String uniqueUserIdentifier,
            @Param("productCodeList") List<String> productCodeList
    );
    @Query(value = "SELECT product_code FROM user_product_info WHERE unique_user_identifier = ?1", nativeQuery = true)
    List<String> findProductCodeByUniqueUserIdentifier(String uniqueUserIdentifier);

    boolean existsByUniqueUserIdentifierAndProductCode(String uniqueUserIdentifier, String productCode);

}
