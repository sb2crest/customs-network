package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.CustomsFdapnSubmit;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface CustomsFdapnSubmitRepository extends JpaRepository<CustomsFdapnSubmit, String> {
    @Query("SELECT c FROM CustomsFdapnSubmit c WHERE c.createdOn = :createdOn AND c.referenceId = :referenceId")
    List<CustomsFdapnSubmit> findByCreatedOnAndReferenceId(@Param("createdOn") Date createdOn, @Param("referenceId") String referenceId);

    List<CustomsFdapnSubmit> findAll(Specification<CustomsFdapnSubmit> specification);


}