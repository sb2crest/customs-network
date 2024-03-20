package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.DailyAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DailyAuditRepository extends JpaRepository<DailyAudit, Long> {
    Optional<DailyAudit> findByUserIdAndDate(String userId, Date date);

    @Query("SELECT da FROM DailyAudit da WHERE da.userId = :userId AND da.date BETWEEN :startDate AND :endDate AND da.date < :endDate")
    List<DailyAudit> findByUserIdAndDateBetween(@Param("userId") String userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
