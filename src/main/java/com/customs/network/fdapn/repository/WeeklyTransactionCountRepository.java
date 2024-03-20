package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.model.WeeklyTransactionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyTransactionCountRepository extends JpaRepository<WeeklyTransactionCount, Long> {
}