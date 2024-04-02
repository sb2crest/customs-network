package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.DailyAudit;
import com.customs.network.fdapn.model.MonthlyAudit;
import com.customs.network.fdapn.model.PortCodeDetails;
import com.customs.network.fdapn.model.PortInfo;
import com.customs.network.fdapn.repository.*;
import com.customs.network.fdapn.utils.DateUtils;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
public class AuditServiceImpl implements AuditService{

    private final DailyAuditRepository dailyAuditRepository;
    private final PortInfoRepository portInfoRepository;
    private final MonthlyAuditRepository monthlyAuditRepository;
    private final PortCodeDetailsRepository portCodeDetailsRepository;
    private final YearlyAuditRepository yearlyAuditRepository;
    @Autowired
    public AuditServiceImpl(DailyAuditRepository dailyAuditRepository, PortInfoRepository portInfoRepository, MonthlyAuditRepository monthlyAuditRepository, PortCodeDetailsRepository portCodeDetailsRepository, YearlyAuditRepository yearlyAuditRepository) {
        this.dailyAuditRepository = dailyAuditRepository;
        this.portInfoRepository = portInfoRepository;
        this.monthlyAuditRepository = monthlyAuditRepository;
        this.portCodeDetailsRepository = portCodeDetailsRepository;
        this.yearlyAuditRepository = yearlyAuditRepository;
    }

    @Override
    public FinalCount<TotalTransactionCountDto<?>> getUserTransactionsForPeriod(String userId, String period) {
        FinalCount<TotalTransactionCountDto<?>> finalCount = new FinalCount<TotalTransactionCountDto<?>>();
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
                case "month"->{
                    return fetchMonthlyAuditTrends(userId);
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
        finalCount.setDailyAuditData(transactions);
        return finalCount;
    }

    private List<DailyAuditDTO> getAuditDataForUser(String userId, Date startDate, Date endDate) {
        List<DailyAudit> auditLists = dailyAuditRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        return auditLists.stream()
                .filter(Objects::nonNull)
                .map(this::convertToMonthlyAuditDto)
                .sorted(Comparator.comparing(DailyAuditDTO::getDate).reversed()) // S
                .toList();
    }
    private DailyAuditDTO getDailyAuditByUserIdAndDate(String userId, Date date) {
        Optional<DailyAudit> userDailyAudit = dailyAuditRepository.findByUserIdAndDate(userId, date);
        if (userDailyAudit.isPresent()) {
            return convertToMonthlyAuditDto(userDailyAudit.get());
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
                    .map(this::convertToMonthlyAuditDto)
                    .sorted(Comparator.comparing(DailyAuditDTO::getDate).reversed())
                    .collect(Collectors.toList());
        }
        else {
            DailyAuditDTO auditDTO = new DailyAuditDTO();
            auditDTO.setUserId(userId);
            return List.of(auditDTO);
        }
    }

    private DailyAuditDTO convertToMonthlyAuditDto(DailyAudit dailyAudit) {
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
    public FinalCount<TotalTransactionCountDto<?>> getAllTransactionsCounts(String userId, String period) {
        FinalCount<TotalTransactionCountDto<?>> finalCount = new FinalCount<TotalTransactionCountDto<?>>();
        List<TotalTransactionCountDto<?>> totalTransactionCountDtos = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));


        if (StringUtils.isBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 3, userId, totalTransactionCountDtos);
        } else if (StringUtils.isNotBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 3, userId, totalTransactionCountDtos);
        } else {
            switch (period) {
                case "today" -> iterateOverDates(calendar, 4, userId, totalTransactionCountDtos);
                case "week" -> iterateOverDates(calendar, 7, userId, totalTransactionCountDtos);
                case "month"-> {
                    return fetchMonthlyAuditTrends(userId);
                }
                default -> throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,"Invalid Option "+period);
            }
        }

        long validationErrorCount = 0, acceptedTotal = 0, rejectedTotal = 0, pendingTotal = 0, cbpDownTotal = 0, overallTotal = 0;
        for (TotalTransactionCountDto<?>  dto : totalTransactionCountDtos) {
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
    public List<PortInfoDto> getPortTransactionInfoByUser(String userId, String portName, String portCode) {
        if (StringUtils.isNotBlank(portCode)) {
            List<PortInfoDto> portInfoDtos = getByPortCode(userId, portCode);
            return nonNull(portInfoDtos) ? portInfoDtos : Collections.emptyList();
        } else if (StringUtils.isNotBlank(portName)) {
            PortCodeDetails portCodeDetails = portCodeDetailsRepository.findByPortName(portName);
            if (isNull(portCodeDetails)) {
                return Collections.emptyList();
            }
            List<PortInfo> portInfoList = portInfoRepository.findByUserIdAndPortNumberOrderByDateDesc(userId, portCodeDetails.getPortCode());
            return mapToPortInfoDtoList(portInfoList);
        } else {
            return Collections.emptyList();
        }
    }
    private List<PortInfoDto> getByPortCode(String userId, String portCode) {
        List<PortInfo> portInfoList = portInfoRepository.findByUserIdAndPortNumberOrderByDateDesc(userId, Integer.valueOf(portCode));
        return mapToPortInfoDtoList(portInfoList);
    }

    private List<PortInfoDto> mapToPortInfoDtoList(List<PortInfo> portInfoList) {
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

    private void iterateOverDates(Calendar calendar, int daysToIterate, String userId, List<TotalTransactionCountDto<?>> totalTransactionCountDtos) {
        for (int i = 0; i < daysToIterate; i++) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -i);
            Date startDate = calendar.getTime();
            List<DailyAudit> dailyAudits = dailyAuditRepository.findByUserIdAndDateRange(userId, startDate, startDate);
            TotalTransactionCountDto<DailyAuditDTO> transactions = getAllTransactionsWithCount(dailyAudits);
            transactions.setDate(DateUtils.formatterDate(startDate));
            totalTransactionCountDtos.add(transactions);
        }
    }

    private TotalTransactionCountDto<DailyAuditDTO> getAllTransactionsWithCount(List<DailyAudit> dailyAudits) {
        TotalTransactionCountDto<DailyAuditDTO> totalTransactionCountDto = new TotalTransactionCountDto<DailyAuditDTO>();
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
        totalTransactionCountDto.setAuditData(dailyAuditDTOS);

        return totalTransactionCountDto;
    }

    @Override
    public void auditAndUpdateMonthlyAuditTable() throws ParseException {
        LocalDate yesterdayLocalDate = LocalDate.now().minusDays(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date yesterday = sdf.parse(yesterdayLocalDate.toString());

        List<DailyAudit> dailyAuditList = dailyAuditRepository.findByDateBetween(yesterday, yesterday);
        for (DailyAudit dailyAudit : dailyAuditList) {
            String monthYear = yesterdayLocalDate.getMonth().name() + " " + yesterdayLocalDate.getYear();
            Optional<MonthlyAudit> monthlyAuditOptional = monthlyAuditRepository.findByUserIdAndMonth(dailyAudit.getUserId(), monthYear);
            MonthlyAudit monthlyAudit;
            if (monthlyAuditOptional.isPresent()) {
                monthlyAudit = monthlyAuditOptional.get();
                monthlyAudit.setAcceptedCount(monthlyAudit.getAcceptedCount() + dailyAudit.getAcceptedCount());
                monthlyAudit.setRejectedCount(monthlyAudit.getRejectedCount() + dailyAudit.getRejectedCount());
                monthlyAudit.setPendingCount(monthlyAudit.getPendingCount() + dailyAudit.getPendingCount());
                monthlyAudit.setValidationErrorCount(monthlyAudit.getValidationErrorCount() + dailyAudit.getValidationErrorCount());
                monthlyAudit.setCbpDownCount(monthlyAudit.getCbpDownCount() + dailyAudit.getCbpDownCount());
                monthlyAudit.setTotalTransactions(monthlyAudit.getTotalTransactions() + dailyAudit.getTotalTransactions());
            } else {
                monthlyAudit = new MonthlyAudit();
                monthlyAudit.setUserId(dailyAudit.getUserId());
                monthlyAudit.setMonth(monthYear);
                monthlyAudit.setAcceptedCount(dailyAudit.getAcceptedCount());
                monthlyAudit.setRejectedCount(dailyAudit.getRejectedCount());
                monthlyAudit.setPendingCount(dailyAudit.getPendingCount());
                monthlyAudit.setValidationErrorCount(dailyAudit.getValidationErrorCount());
                monthlyAudit.setCbpDownCount(dailyAudit.getCbpDownCount());
                monthlyAudit.setTotalTransactions(dailyAudit.getTotalTransactions());
            }
            monthlyAuditRepository.save(monthlyAudit);
        }
    }

    public void auditAndUpdateYearlyAuditTable(){

    }

    private FinalCount<TotalTransactionCountDto<?>> fetchMonthlyAuditTrends(String userId) {
        FinalCount<TotalTransactionCountDto<?>> finalCount = new FinalCount<>();
        Map<String, TotalTransactionCountDto<?>> trendsData = new LinkedHashMap<>();
        long totalValidationErrorCount = 0;
        long totalAcceptedCount = 0;
        long totalRejectedCount = 0;
        long totalPendingCount = 0;
        long totalCbpDownCount = 0;
        long allTransactions = 0;

        LocalDate now = LocalDate.now();

            for (int i = 0; i <6; i++) {
                LocalDate month = now.minusMonths(i);
                String monthYear = month.getMonth().name() + " " + month.getYear();
                List<MonthlyAudit> monthlyAudits = monthlyAuditRepository.findAllByMonthAndUserId(monthYear, userId);
                TotalTransactionCountDto<MonthlyAuditDto> totalTransactionCountDto = calculateTotalTransactionCounts(monthlyAudits);
                String[] parts = monthYear.split(" ");
                String formattedMonth= (parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase()).substring(0,3);
                String year = parts[1];
                String formattedMonthYear = formattedMonth + " " + year;
                trendsData.put(formattedMonthYear, totalTransactionCountDto);
                totalValidationErrorCount += totalTransactionCountDto.getValidationErrorCount();
                totalAcceptedCount += totalTransactionCountDto.getAcceptedCount();
                totalRejectedCount += totalTransactionCountDto.getRejectedCount();
                totalPendingCount += totalTransactionCountDto.getPendingCount();
                totalCbpDownCount += totalTransactionCountDto.getCbpDownCount();
                allTransactions += totalTransactionCountDto.getTotalTransactions();
            }


        finalCount.setTotalValidationErrorCount(totalValidationErrorCount);
        finalCount.setTotalAcceptedCount(totalAcceptedCount);
        finalCount.setTotalRejectedCount(totalRejectedCount);
        finalCount.setTotalPendingCount(totalPendingCount);
        finalCount.setTotalCbpDownCount(totalCbpDownCount);
        finalCount.setAllTransactions(allTransactions);
        finalCount.setTrendsData(trendsData);
        return finalCount;
    }

    private TotalTransactionCountDto<MonthlyAuditDto> calculateTotalTransactionCounts(List<MonthlyAudit> monthlyAudits) {
        TotalTransactionCountDto<MonthlyAuditDto> totalTransactionCountDto = new TotalTransactionCountDto<>();
        for (MonthlyAudit monthlyAudit : monthlyAudits) {
            totalTransactionCountDto.setAcceptedCount(totalTransactionCountDto.getAcceptedCount() + monthlyAudit.getAcceptedCount());
            totalTransactionCountDto.setRejectedCount(totalTransactionCountDto.getRejectedCount() + monthlyAudit.getRejectedCount());
            totalTransactionCountDto.setPendingCount(totalTransactionCountDto.getPendingCount() + monthlyAudit.getPendingCount());
            totalTransactionCountDto.setValidationErrorCount(totalTransactionCountDto.getValidationErrorCount() + monthlyAudit.getValidationErrorCount());
            totalTransactionCountDto.setCbpDownCount(totalTransactionCountDto.getCbpDownCount() + monthlyAudit.getCbpDownCount());
            totalTransactionCountDto.setTotalTransactions(totalTransactionCountDto.getTotalTransactions() + monthlyAudit.getTotalTransactions());
            totalTransactionCountDto.setMonth(monthlyAudit.getMonth());
        }   totalTransactionCountDto.setAuditData(monthlyAudits.stream().map(this::convertToMonthlyAuditDto).toList());
        return totalTransactionCountDto;
    }

    private MonthlyAuditDto convertToMonthlyAuditDto(MonthlyAudit monthlyAudit) {
        MonthlyAuditDto monthlyAuditDto = new MonthlyAuditDto();
        monthlyAuditDto.setId(monthlyAudit.getId());
        monthlyAuditDto.setUserId(monthlyAudit.getUserId());
        monthlyAuditDto.setMonth(monthlyAudit.getMonth());
        monthlyAuditDto.setAcceptedCount(monthlyAudit.getAcceptedCount());
        monthlyAuditDto.setRejectedCount(monthlyAudit.getRejectedCount());
        monthlyAuditDto.setPendingCount(monthlyAudit.getPendingCount());
        monthlyAuditDto.setValidationErrorCount(monthlyAudit.getValidationErrorCount());
        monthlyAuditDto.setCbpDownCount(monthlyAudit.getCbpDownCount());
        monthlyAuditDto.setTotalTransactions(monthlyAudit.getTotalTransactions());
        return monthlyAuditDto;
    }
}
