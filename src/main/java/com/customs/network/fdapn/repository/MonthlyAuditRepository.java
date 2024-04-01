package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.MonthlyAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyAuditRepository extends JpaRepository<MonthlyAudit,Long> {
   List<MonthlyAudit> findByMonth(String month);
   @Query("SELECT ma FROM MonthlyAudit ma WHERE ma.userId = :userId AND ma.month >= :startMonth AND ma.month <= :endMonth ORDER BY ma.month DESC")
   List<MonthlyAudit> findMonthlyAuditsForUserId(@Param("userId") String userId, @Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

   Optional<MonthlyAudit> findByUserIdAndMonth(String userId, String monthYear);

   List<MonthlyAudit> findByUserId(String userId);

   @Query("SELECT ma FROM MonthlyAudit ma WHERE ma.month = :month" +
           " AND (:userId IS NULL OR ma.userId = :userId)")
   List<MonthlyAudit> findAllByMonthAndUserId(@Param("month") String month, @Param("userId") String userId);

}
