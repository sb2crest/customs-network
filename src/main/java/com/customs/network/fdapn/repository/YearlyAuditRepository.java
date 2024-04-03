package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.YearlyAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface YearlyAuditRepository extends JpaRepository<YearlyAudit,Long> {
    @Query("SELECT ya FROM YearlyAudit ya WHERE (:userId IS NULL OR ya.userId = :userId) AND ya.year = :year")
    List<YearlyAudit> findAllByYearAndUserId(@Param("year") String year, @Param("userId") String userId);
    Optional<YearlyAudit> findByYearAndUserId(@Param("year") String year, @Param("userId") String userId);

}
