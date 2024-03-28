package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.PortInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortInfoRepository extends JpaRepository<PortInfo, Long> {
    List<PortInfo> findByUserIdAndPortNumberOrderByDateDesc(String userId,String portCode);
}
