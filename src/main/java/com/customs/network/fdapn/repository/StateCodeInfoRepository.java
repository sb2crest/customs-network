package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.StateCodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StateCodeInfoRepository extends JpaRepository<StateCodeInfo,String> {
    @Query("SELECT s.countryCode FROM StateCodeInfo s")
    List<String> findAllCountryCodes();
}
