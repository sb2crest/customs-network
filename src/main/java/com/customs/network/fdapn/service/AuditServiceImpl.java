package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.DailyAudit;
import com.customs.network.fdapn.model.PortInfo;
import com.customs.network.fdapn.repository.DailyAuditRepository;
import com.customs.network.fdapn.repository.PortInfoRepository;
import com.customs.network.fdapn.utils.DateUtils;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditServiceImpl implements AuditService{

    private final DailyAuditRepository dailyAuditRepository;
    private final PortInfoRepository portInfoRepository;

    @Autowired
    public AuditServiceImpl(DailyAuditRepository dailyAuditRepository, PortInfoRepository portInfoRepository) {
        this.dailyAuditRepository = dailyAuditRepository;
        this.portInfoRepository = portInfoRepository;
    }

    @Override
    public FinalCountForUser getUserTransactionsForPeriod(String userId, String period) {
        FinalCountForUser finalCount = new FinalCountForUser();
        if(StringUtils.isEmpty(userId)){
            throw new FdapnCustomExceptions(ErrorResCodes.NOT_FOUND,"userId is not provided");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date endDate = calendar.getTime();
        List<DailyAuditDTO> transactions;
        if (StringUtils.isEmpty(period)) {
            transactions = Collections.singletonList(getDailyAuditByUserIdAndDate(userId, endDate));
        } else {
            switch (period) {
                case "today" -> {
                    calendar.add(Calendar.DATE, -3);
                    Date startDateToday = calendar.getTime();
                    transactions = getDailyAuditByUserId(userId, startDateToday, endDate);
                }
                case "week" -> {
                    calendar.add(Calendar.DATE, -7);
                    Date startDateWeek = calendar.getTime();
                    transactions = getAuditDataForUser(userId, startDateWeek, endDate);
                }
                default -> throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,"Invalid Option "+period);
            }
        }
        long validationErrorCount = 0, acceptedTotal = 0, rejectedTotal = 0, pendingTotal = 0, cbpDownTotal = 0, overallTotal = 0;
        for (DailyAuditDTO dto : transactions) {
            validationErrorCount += dto.getValidationErrorCount();
            acceptedTotal += dto.getAcceptedCount();
            rejectedTotal += dto.getRejectedCount();
            pendingTotal += dto.getPendingCount();
            cbpDownTotal += dto.getCbpDownCount();
            overallTotal += dto.getTotalTransactions();
        }

        finalCount.setTotalValidationErrorCount(validationErrorCount);
        finalCount.setTotalAcceptedCount(acceptedTotal);
        finalCount.setTotalRejectedCount(rejectedTotal);
        finalCount.setTotalPendingCount(pendingTotal);
        finalCount.setTotalCbpDownCount(cbpDownTotal);
        finalCount.setAllTransactions(overallTotal);
        finalCount.setDailyAuditDTOS(transactions);
        return finalCount;
    }
    private List<DailyAuditDTO> getAuditDataForUser(String userId, Date startDate, Date endDate) {
        List<DailyAudit> auditLists = dailyAuditRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return auditLists.stream()
                .filter(Objects::nonNull)
                .map(this::convertToDto)
                .sorted(Comparator.comparing(DailyAuditDTO::getDate).reversed()) // S
                .toList();
    }
    private DailyAuditDTO getDailyAuditByUserIdAndDate(String userId, Date date) {
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
    private List<DailyAuditDTO> getDailyAuditByUserId(String userId, Date startDate,Date endDate) {
        List<DailyAudit> userDailyAudit = dailyAuditRepository.findByUserIdAndDateRange(userId, startDate,endDate);
        if (!userDailyAudit.isEmpty()) {
           return userDailyAudit.stream()
                    .filter(Objects::nonNull)
                    .map(this::convertToDto)
                    .sorted(Comparator.comparing(DailyAuditDTO::getDate).reversed())
                    .collect(Collectors.toList());
        }
        else {
            DailyAuditDTO auditDTO = new DailyAuditDTO();
            auditDTO.setUserId(userId);
            return List.of(auditDTO);
        }
    }

    private DailyAuditDTO convertToDto(DailyAudit dailyAudit) {
        DailyAuditDTO dto = new DailyAuditDTO();
        dto.setId(dailyAudit.getId());
        dto.setUserId(dailyAudit.getUserId());
        dto.setDate(DateUtils.formatterDate(dailyAudit.getDate()));
        dto.setValidationErrorCount(dailyAudit.getValidationErrorCount());
        dto.setAcceptedCount(dailyAudit.getAcceptedCount());
        dto.setRejectedCount(dailyAudit.getRejectedCount());
        dto.setPendingCount(dailyAudit.getPendingCount());
        dto.setCbpDownCount(dailyAudit.getCbpDownCount());
        dto.setTotalTransactions(dailyAudit.getTotalTransactions());
        return dto;
    }
    @Override
    public FinalCount getAllTransactionsCounts(String userId, String period) {
        FinalCount finalCount = new FinalCount();
        List<TotalTransactionCountDto> totalTransactionCountDtos = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date endDate = calendar.getTime();
        List<DailyAudit> dailyAudits = null;

        if (StringUtils.isBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 4, userId, totalTransactionCountDtos);
        } else if (StringUtils.isNotBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 4, userId, totalTransactionCountDtos);
        } else {
            switch (period) {
                case "today" -> iterateOverDates(calendar, 4, userId, totalTransactionCountDtos);
                case "week" -> iterateOverDates(calendar, 7, userId, totalTransactionCountDtos);
                default -> throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,"Invalid Option "+period);
            }
        }

        long validationErrorCount = 0, acceptedTotal = 0, rejectedTotal = 0, pendingTotal = 0, cbpDownTotal = 0, overallTotal = 0;
        for (TotalTransactionCountDto  dto : totalTransactionCountDtos) {
            validationErrorCount += dto.getValidationErrorCount();
            acceptedTotal += dto.getAcceptedCount();
            rejectedTotal += dto.getRejectedCount();
            pendingTotal += dto.getPendingCount();
            cbpDownTotal += dto.getCbpDownCount();
            overallTotal += dto.getTotalTransactions();
        }

        finalCount.setTotalValidationErrorCount(validationErrorCount);
        finalCount.setTotalAcceptedCount(acceptedTotal);
        finalCount.setTotalRejectedCount(rejectedTotal);
        finalCount.setTotalPendingCount(pendingTotal);
        finalCount.setTotalCbpDownCount(cbpDownTotal);
        finalCount.setAllTransactions(overallTotal);
        finalCount.setTotalTransactionCountDtos(totalTransactionCountDtos);
        return finalCount;
    }

    @Override
    public List<PortInfoDto> getByUser(String userId, String portCode) {
        List<PortInfo> portInfoList = portInfoRepository.findByUserIdAndPortNumberOrderByDateDesc(userId,portCode);
        return portInfoList.stream()
                .filter(Objects::nonNull)
                .map(entity -> {
                    PortInfoDto portInfoDto = new PortInfoDto();
                    portInfoDto.setSno(entity.getSno());
                    portInfoDto.setPortCode(entity.getPortNumber());
                    portInfoDto.setUserId(entity.getUserId());
                    portInfoDto.setDate(DateUtils.formatterDate(entity.getDate()));
                    portInfoDto.setAcceptedCount(entity.getAcceptedCount());
                    portInfoDto.setPendingCount(entity.getPendingCount());
                    portInfoDto.setRejectedCount(entity.getRejectedCount());
                    portInfoDto.setTotalCount(entity.getTotalCount());
                    return portInfoDto;
                })
                .toList();
    }

    private void iterateOverDates(Calendar calendar, int daysToIterate, String userId, List<TotalTransactionCountDto> totalTransactionCountDtos) {
        for (int i = 0; i < daysToIterate; i++) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -i);
            Date startDate = calendar.getTime();
            Date endDate = startDate;
            List<DailyAudit> dailyAudits = dailyAuditRepository.findByUserIdAndDateRange(userId, startDate, endDate);
            TotalTransactionCountDto transactions = getAllTransactionsWithCount(dailyAudits);
            transactions.setDate(DateUtils.formatterDate(startDate));
            totalTransactionCountDtos.add(transactions);
        }
    }



    private TotalTransactionCountDto getAllTransactionsWithCount(List<DailyAudit> dailyAudits) {
        TotalTransactionCountDto totalTransactionCountDto = new TotalTransactionCountDto();
        List<DailyAuditDTO> dailyAuditDTOS = new ArrayList<>();
        long validationErrorTotal=0, acceptedTotal = 0, rejectedTotal = 0, pendingTotal = 0, cbpDownTotal = 0, overallTotal = 0;

        for (DailyAudit audit : dailyAudits) {
            if (audit != null) {
                Date createdOn = audit.getDate();
                long validationErrorCount = audit.getValidationErrorCount();
                long acceptedCount = audit.getAcceptedCount();
                long rejectedCount = audit.getRejectedCount();
                long pendingCount = audit.getPendingCount();
                long cbpDownCount = audit.getCbpDownCount();
                long totalTransactions = audit.getTotalTransactions();

                validationErrorTotal += validationErrorCount;
                acceptedTotal += acceptedCount;
                rejectedTotal += rejectedCount;
                pendingTotal += pendingCount;
                cbpDownTotal += cbpDownCount;
                overallTotal += totalTransactions;

                DailyAuditDTO dailyAuditDTO = new DailyAuditDTO();
                dailyAuditDTO.setId(audit.getId());
                dailyAuditDTO.setUserId(audit.getUserId());
                dailyAuditDTO.setDate(DateUtils.formatterDate(createdOn));
                dailyAuditDTO.setValidationErrorCount(validationErrorCount);
                dailyAuditDTO.setAcceptedCount(acceptedCount);
                dailyAuditDTO.setRejectedCount(rejectedCount);
                dailyAuditDTO.setPendingCount(pendingCount);
                dailyAuditDTO.setCbpDownCount(cbpDownCount);
                dailyAuditDTO.setTotalTransactions(totalTransactions);

                dailyAuditDTOS.add(dailyAuditDTO);
            }
        }

        totalTransactionCountDto.setValidationErrorCount(validationErrorTotal);
        totalTransactionCountDto.setAcceptedCount(acceptedTotal);
        totalTransactionCountDto.setRejectedCount(rejectedTotal);
        totalTransactionCountDto.setPendingCount(pendingTotal);
        totalTransactionCountDto.setCbpDownCount(cbpDownTotal);
        totalTransactionCountDto.setTotalTransactions(overallTotal);
        totalTransactionCountDto.setDailyAuditDTOS(dailyAuditDTOS);

        return totalTransactionCountDto;
    }

}