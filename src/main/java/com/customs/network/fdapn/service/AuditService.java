package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.DailyAuditDTO;
import com.customs.network.fdapn.model.DailyAudit;
import com.customs.network.fdapn.repository.DailyAuditRepository;
import com.customs.network.fdapn.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuditService {

    private final DailyAuditRepository dailyAuditRepository;

    @Autowired
    public AuditService(DailyAuditRepository dailyAuditRepository) {
        this.dailyAuditRepository = dailyAuditRepository;
    }

    public List<DailyAuditDTO> getAuditDataForUser(String userId, Date startDate, Date endDate) {
        List<DailyAudit> auditLists = dailyAuditRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return auditLists.stream()
                .filter(Objects::nonNull)
                .map(this::convertToDto)
                .toList();
    }
    public DailyAuditDTO getDailyAuditByUserIdAndDate(String userId, Date date) {
        DailyAudit dailyAudit = dailyAuditRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new NoSuchElementException("No audit record found for userId " + userId));
        return convertToDto(dailyAudit);
    }

    private DailyAuditDTO convertToDto(DailyAudit dailyAudit) {
        DailyAuditDTO dto = new DailyAuditDTO();
        dto.setId(dailyAudit.getId());
        dto.setUserId(dailyAudit.getUserId());
        dto.setDate(DateUtils.formatterDate(dailyAudit.getDate()));
        dto.setSuccessCount(dailyAudit.getAcceptedCount());
        dto.setFailedCount(dailyAudit.getRejectedCount());
        dto.setPendingCount(dailyAudit.getPendingCount());
        dto.setCbpDownCount(dailyAudit.getCbpDownCount());
        dto.setTotalTransactions(dailyAudit.getTotalTransactions());
        return dto;
    }


}
