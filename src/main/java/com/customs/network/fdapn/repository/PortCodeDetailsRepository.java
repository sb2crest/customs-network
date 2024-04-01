package com.customs.network.fdapn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import com.customs.network.fdapn.model.PortCodeDetails;

public interface PortCodeDetailsRepository extends JpaRepository<PortCodeDetails, Long> {

    @Query("SELECT DISTINCT p.portName FROM PortCodeDetails p " +
            "WHERE (:portNamePattern IS NULL OR LOWER(SUBSTRING(p.portName, LENGTH(p.portName) - 3)) LIKE CONCAT('%', LOWER(:portNamePattern), '%')) " +
            "ORDER BY p.portName DESC")
    List<String> findDistinctPortNamesByPattern(@Param("portNamePattern") String portNamePattern);


    @Query("SELECT DISTINCT p.portCode FROM PortCodeDetails p " +
            "WHERE (:portCodePattern IS NULL OR SUBSTRING(CAST(p.portCode AS string), 1, 2) LIKE CONCAT('%', :portCodePattern, '%')) " +
            "ORDER BY p.portCode DESC")
    List<Integer> findDistinctPortCodesByPattern(@Param("portCodePattern") Integer portCodePattern);

}
