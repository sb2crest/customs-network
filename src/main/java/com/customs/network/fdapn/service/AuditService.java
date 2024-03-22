package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.DailyAuditDTO;
import com.customs.network.fdapn.dto.TotalTransactionCountDto;
import com.customs.network.fdapn.model.DailyAudit;
import com.customs.network.fdapn.repository.DailyAuditRepository;
import com.customs.network.fdapn.utils.DateUtils;
import io.micrometer.common.util.StringUtils;
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
        Optional<DailyAudit> userDailyAudit = dailyAuditRepository.findByUserIdAndDate(userId, date);
        if (userDailyAudit.isPresent()) {
            return convertToDto(userDailyAudit.get());
        } else {
            DailyAuditDTO auditDTO = new DailyAuditDTO();
            auditDTO.setUserId(userId);
            auditDTO.setDate(DateUtils.formatterDate(date));
            return auditDTO;
        }
    }

    private DailyAuditDTO convertToDto(DailyAudit dailyAudit) {
        DailyAuditDTO dto = new DailyAuditDTO();
        dto.setId(dailyAudit.getId());
        dto.setUserId(dailyAudit.getUserId());
        dto.setDate(DateUtils.formatterDate(dailyAudit.getDate()));
        dto.setAcceptedCount(dailyAudit.getAcceptedCount());
        dto.setRejectedCount(dailyAudit.getRejectedCount());
        dto.setPendingCount(dailyAudit.getPendingCount());
        dto.setCbpDownCount(dailyAudit.getCbpDownCount());
        dto.setTotalTransactions(dailyAudit.getTotalTransactions());
        return dto;
    }
    public TotalTransactionCountDto getAllTransactionsCounts(String userId, String period) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DATE, -7);
        Date startDate = calendar.getTime();

        TotalTransactionCountDto transactions;
        List<DailyAudit> dailyAudits;

        if (StringUtils.isBlank(userId) && StringUtils.isBlank(period)) {
            dailyAudits = dailyAuditRepository.findByDateBetween(endDate,endDate);
        } else if (StringUtils.isNotBlank(userId) && StringUtils.isBlank(period)) {
            dailyAudits = Collections.singletonList(dailyAuditRepository.findByUserIdAndDate(userId, endDate).orElseThrow());
        } else {
            dailyAudits = switch (period) {
                case "today" ->
                      dailyAuditRepository.findByUserIdAndDateRange(userId,endDate,endDate);
                case "week" -> dailyAuditRepository.findByUserIdAndDateRange(userId, startDate, endDate);
                default -> throw new RuntimeException("invalid period");
            };
        }
        transactions = getAllTransactionsWithCount(dailyAudits);
        return transactions;
    }
    public TotalTransactionCountDto getAllTransactionsWithCount(List<DailyAudit> dailyAudits) {
        TotalTransactionCountDto totalTransactionCountDto = new TotalTransactionCountDto();
        List<DailyAuditDTO> dailyAuditDTOS = new ArrayList<>();
        long acceptedTotal = 0, rejectedTotal = 0, pendingTotal = 0, cbpDownTotal = 0, overallTotal = 0;

        for (DailyAudit audit : dailyAudits) {
            if (audit != null) {
                Date createdOn = audit.getDate();
                long acceptedCount = audit.getAcceptedCount();
                long rejectedCount = audit.getRejectedCount();
                long pendingCount = audit.getPendingCount();
                long cbpDownCount = audit.getCbpDownCount();
                long totalTransactions = audit.getTotalTransactions();

                acceptedTotal += acceptedCount;
                rejectedTotal += rejectedCount;
                pendingTotal += pendingCount;
                cbpDownTotal += cbpDownCount;
                overallTotal += totalTransactions;

                DailyAuditDTO dailyAuditDTO = new DailyAuditDTO();
                dailyAuditDTO.setId(audit.getId());
                dailyAuditDTO.setUserId(audit.getUserId());
                dailyAuditDTO.setDate(DateUtils.formatterDate(createdOn));
                dailyAuditDTO.setAcceptedCount(acceptedCount);
                dailyAuditDTO.setRejectedCount(rejectedCount);
                dailyAuditDTO.setPendingCount(pendingCount);
                dailyAuditDTO.setCbpDownCount(cbpDownCount);
                dailyAuditDTO.setTotalTransactions(totalTransactions);

                dailyAuditDTOS.add(dailyAuditDTO);
            }
        }

        totalTransactionCountDto.setAcceptedCount(acceptedTotal);
        totalTransactionCountDto.setRejectedCount(rejectedTotal);
        totalTransactionCountDto.setPendingCount(pendingTotal);
        totalTransactionCountDto.setCbpDownCount(cbpDownTotal);
        totalTransactionCountDto.setTotalTransactions(overallTotal);
        totalTransactionCountDto.setDailyAuditDTOS(dailyAuditDTOS);

        return totalTransactionCountDto;
    }

}