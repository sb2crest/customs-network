package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.*;
import com.customs.network.fdapn.repository.*;
import com.customs.network.fdapn.service.AuditService;
import com.customs.network.fdapn.utils.DateUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static java.util.Objects.isNull;

@Service
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final DailyAuditRepository dailyAuditRepository;
    private final PortInfoRepository portInfoRepository;
    private final MonthlyAuditRepository monthlyAuditRepository;
    private final PortCodeDetailsRepository portCodeDetailsRepository;
    private final YearlyAuditRepository yearlyAuditRepository;

    @Autowired
    public AuditServiceImpl(DailyAuditRepository dailyAuditRepository,
                            PortInfoRepository portInfoRepository,
                            MonthlyAuditRepository monthlyAuditRepository,
                            PortCodeDetailsRepository portCodeDetailsRepository,
                            YearlyAuditRepository yearlyAuditRepository) {
        this.dailyAuditRepository = dailyAuditRepository;
        this.portInfoRepository = portInfoRepository;
        this.monthlyAuditRepository = monthlyAuditRepository;
        this.portCodeDetailsRepository = portCodeDetailsRepository;
        this.yearlyAuditRepository = yearlyAuditRepository;
    }

    @Override
    public FinalCount<TotalTransactionCountDto<?>> getUserTransactionsForPeriod(String userId, String period) {
        FinalCount<TotalTransactionCountDto<?>> finalCount = new FinalCount<>();
        if (StringUtils.isEmpty(userId)) {
            throw new FdapnCustomExceptions(ErrorResCodes.NOT_FOUND, "userId is not provided");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date endDate = calendar.getTime();
        List<DailyAuditDTO> transactions;
        if (StringUtils.isEmpty(period)) {
            transactions = Collections.singletonList(getDailyAuditByUserIdAndDate(userId, endDate));
        } else {
            switch (period) {
                case "today" -> transactions = fetchLastThreeDaysAuditTrends(userId);
                case "week" -> transactions = fetchWeeklyAuditTrends(userId);
                case "month" -> {
                    return fetchMonthlyAuditTrends(userId);
                }
                case "year" -> {
                    return fetchYearlyAuditTrends(userId);
                }
                default -> throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "Invalid Option " + period);
            }
        }
        long validationErrorCount = 0;
        long acceptedTotal = 0;
        long rejectedTotal = 0;
        long pendingTotal = 0;
        long cbpDownTotal = 0;
        long overallTotal = 0;
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

    private List<DailyAuditDTO> fetchWeeklyAuditTrends(String userId) {
        List<DailyAuditDTO> dailyAuditList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.minusDays(1);
        LocalDate startDate = endDate.minusDays(6);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Date currentDate1 = java.sql.Date.valueOf(date);
            List<DailyAuditDTO> dailyAuditForDay = getDailyAuditBasedOnDayBack(userId, currentDate1);
            dailyAuditList.addAll(dailyAuditForDay);
        }
        dailyAuditList.sort(Comparator.comparing(DailyAuditDTO::getDate).reversed());
        return dailyAuditList;
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

    public List<DailyAuditDTO> fetchLastThreeDaysAuditTrends(String userId) {
        List<DailyAuditDTO> dailyAuditList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.minusDays(1);
        LocalDate startDate = endDate.minusDays(2);
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            Date currentDate1 = java.sql.Date.valueOf(date);
            List<DailyAuditDTO> dailyAuditForDay = getDailyAuditBasedOnDayBack(userId, currentDate1);
            dailyAuditList.addAll(dailyAuditForDay);
        }
        dailyAuditList.sort(Comparator.comparing(DailyAuditDTO::getDate).reversed());
        return dailyAuditList;
    }

    public List<DailyAuditDTO> getDailyAuditBasedOnDayBack(String userId, Date date) {
        Optional<DailyAudit> userDailyAudit = dailyAuditRepository.findByUserIdAndDate(userId, date);
        List<DailyAuditDTO> dailyAuditList = new ArrayList<>();
        if (userDailyAudit.isPresent()) {
            dailyAuditList.addAll(userDailyAudit.stream()
                    .filter(Objects::nonNull)
                    .map(this::convertToMonthlyAuditDto)
                    .toList());
        } else {
            DailyAuditDTO auditDTO = new DailyAuditDTO();
            auditDTO.setUserId(userId);
            auditDTO.setDate(DateUtils.formatterDate(date));
            dailyAuditList.add(auditDTO);
        }
        return dailyAuditList;
    }

    @Override
    @Cacheable(value = "transactionsCache", key = "#userId + '_' + #period")
    public FinalCount<TotalTransactionCountDto<?>> getAllTransactionsCounts(String userId, String period) {
        log.info("Fetching from database");
        List<TotalTransactionCountDto<?>> totalTransactionCountList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (StringUtils.isBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 3, userId, totalTransactionCountList);
        } else if (StringUtils.isNotBlank(userId) && StringUtils.isBlank(period)) {
            iterateOverDates(calendar, 3, userId, totalTransactionCountList);
        } else {
            switch (period) {
                case "today" -> iterateOverDates(calendar, 3, userId, totalTransactionCountList);
                case "week" -> iterateOverDates(calendar, 7, userId, totalTransactionCountList);
                case "month" -> {
                    return fetchMonthlyAuditTrends(userId);
                }
                case "year" -> {
                    return fetchYearlyAuditTrends(userId);
                }
                default -> throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "Invalid Option " + period);
            }
        }
        FinalCount<TotalTransactionCountDto<?>> finalCount = calculateFinalCount(totalTransactionCountList);
        finalCount.setTotalTransactionCountDtos(totalTransactionCountList);
        return finalCount;
    }

    @Override
    public List<PortInfoDto> getPortTransactionInfoByUser(String userId, String portName, String portCode) {
        if (StringUtils.isNotBlank(portCode)) {
            return getByPortCode(userId, portCode);
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
        List<PortInfo> portInfoList = portInfoRepository.findByUserIdAndPortNumberOrderByDateDesc(userId, portCode);
        if (portInfoList.isEmpty())
            return Collections.emptyList();
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

    private void iterateOverDates(Calendar calendar, int daysToIterate, String userId, List<TotalTransactionCountDto<?>> totalTransactionCountList) {
        for (int i = 0; i < daysToIterate; i++) {
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, -i);
            Date startDate = calendar.getTime();
            List<DailyAudit> dailyAudits = dailyAuditRepository.findByUserIdAndDateRange(userId, startDate, startDate);
            TotalTransactionCountDto<DailyAuditDTO> transactions = getAllTransactionsWithCountForDaily(dailyAudits);
            transactions.setDate(DateUtils.formatterDate(startDate));
            totalTransactionCountList.add(transactions);
        }
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

    @Override
    public void auditAndUpdateYearlyAuditTable() {
        LocalDate now = LocalDate.now();
        LocalDate month = now.minusMonths(1);
        String monthYear = month.getMonth().name() + " " + month.getYear();
        List<MonthlyAudit> monthlyAudits = monthlyAuditRepository.findByMonth(monthYear);

        monthlyAudits.stream()
                .filter(Objects::nonNull)
                .forEach(monthlyAudit -> {
                    String[] monthYearArray = monthlyAudit.getMonth().split(" ");
                    String year = monthYearArray[1];
                    Optional<YearlyAudit> yearlyAuditOptional = yearlyAuditRepository.findByYearAndUserId(year, monthlyAudit.getUserId());
                    YearlyAudit yearlyAudit;
                    if (yearlyAuditOptional.isPresent()) {
                        yearlyAudit = yearlyAuditOptional.get();
                        yearlyAudit.setAcceptedCount(yearlyAudit.getAcceptedCount() + monthlyAudit.getAcceptedCount());
                        yearlyAudit.setRejectedCount(yearlyAudit.getRejectedCount() + monthlyAudit.getRejectedCount());
                        yearlyAudit.setPendingCount(yearlyAudit.getPendingCount() + monthlyAudit.getPendingCount());
                        yearlyAudit.setValidationErrorCount(yearlyAudit.getValidationErrorCount() + monthlyAudit.getValidationErrorCount());
                        yearlyAudit.setCbpDownCount(yearlyAudit.getCbpDownCount() + monthlyAudit.getCbpDownCount());
                        yearlyAudit.setTotalTransactions(yearlyAudit.getTotalTransactions() + monthlyAudit.getTotalTransactions());
                        yearlyAuditRepository.save(yearlyAudit);
                    } else {
                        yearlyAudit = new YearlyAudit();
                        yearlyAudit.setUserId(monthlyAudit.getUserId());
                        yearlyAudit.setYear(year);
                        yearlyAudit.setAcceptedCount(monthlyAudit.getAcceptedCount());
                        yearlyAudit.setRejectedCount(monthlyAudit.getRejectedCount());
                        yearlyAudit.setPendingCount(monthlyAudit.getPendingCount());
                        yearlyAudit.setValidationErrorCount(monthlyAudit.getValidationErrorCount());
                        yearlyAudit.setCbpDownCount(monthlyAudit.getCbpDownCount());
                        yearlyAudit.setTotalTransactions(monthlyAudit.getTotalTransactions());
                        yearlyAuditRepository.save(yearlyAudit);
                    }
                });
    }


    private FinalCount<TotalTransactionCountDto<?>> fetchMonthlyAuditTrends(String userId) {
        Map<String, TotalTransactionCountDto<?>> trendsData = new LinkedHashMap<>();
        List<TotalTransactionCountDto<?>> transactionCountDtoList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < 6; i++) {
            LocalDate month = now.minusMonths(i);
            String monthYear = month.getMonth().name() + " " + month.getYear();
            List<MonthlyAudit> monthlyAudits = monthlyAuditRepository.findAllByMonthAndUserId(monthYear, userId);
            TotalTransactionCountDto<MonthlyAuditDto> totalTransactionCountDto = calculateTotalTransactionCountsForMonth(monthlyAudits);
            String[] parts = monthYear.split(" ");
            String formattedMonth = (parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1).toLowerCase()).substring(0, 3);
            String year = parts[1];
            String formattedMonthYear = formattedMonth + " " + year;
            trendsData.put(formattedMonthYear, totalTransactionCountDto);
            transactionCountDtoList.add(totalTransactionCountDto);
        }
        FinalCount<TotalTransactionCountDto<?>> finalCount = calculateFinalCount(transactionCountDtoList);
        finalCount.setTrendsData(trendsData);
        return finalCount;
    }

    private FinalCount<TotalTransactionCountDto<?>> fetchYearlyAuditTrends(String userId) {
        Map<String, TotalTransactionCountDto<?>> trendsData = new LinkedHashMap<>();
        List<TotalTransactionCountDto<?>> transactionCountDtoList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        int start=0;
        int end =5;
        for (int i = start; i < end; i++) {
            int year = now.minusYears(i).getYear();
            List<YearlyAudit> yearlyAudits = yearlyAuditRepository.findAllByYearAndUserId(String.valueOf(year), userId);
            TotalTransactionCountDto<YearlyAuditDto> totalTransactionCountDto = calculateTotalTransactionCountsForYear(yearlyAudits);
            trendsData.put(String.valueOf(year), totalTransactionCountDto);
            transactionCountDtoList.add(totalTransactionCountDto);
        }
        FinalCount<TotalTransactionCountDto<?>> finalCount = calculateFinalCount(transactionCountDtoList);
        finalCount.setTrendsData(trendsData);
        return finalCount;
    }

    private FinalCount<TotalTransactionCountDto<?>> calculateFinalCount(List<TotalTransactionCountDto<?>> totalTransactionCountDtoList) {
        FinalCount<TotalTransactionCountDto<?>> finalCount = new FinalCount<>();
        long totalValidationErrorCount = 0;
        long totalAcceptedCount = 0;
        long totalRejectedCount = 0;
        long totalPendingCount = 0;
        long totalCbpDownCount = 0;
        long allTransactions = 0;
        for (TotalTransactionCountDto<?> transactions : totalTransactionCountDtoList) {
            totalValidationErrorCount += transactions.getValidationErrorCount();
            totalAcceptedCount += transactions.getAcceptedCount();
            totalRejectedCount += transactions.getRejectedCount();
            totalPendingCount += transactions.getPendingCount();
            totalCbpDownCount += transactions.getCbpDownCount();
            allTransactions += transactions.getTotalTransactions();
        }
        finalCount.setTotalValidationErrorCount(totalValidationErrorCount);
        finalCount.setTotalAcceptedCount(totalAcceptedCount);
        finalCount.setTotalRejectedCount(totalRejectedCount);
        finalCount.setTotalPendingCount(totalPendingCount);
        finalCount.setTotalCbpDownCount(totalCbpDownCount);
        finalCount.setAllTransactions(allTransactions);
        return finalCount;
    }

    private TotalTransactionCountDto<MonthlyAuditDto> calculateTotalTransactionCountsForMonth(List<MonthlyAudit> monthlyAudits) {
        TotalTransactionCountDto<MonthlyAuditDto> totalTransactionCountDto = new TotalTransactionCountDto<>();
        for (MonthlyAudit monthlyAudit : monthlyAudits) {
            totalTransactionCountDto.setAcceptedCount(totalTransactionCountDto.getAcceptedCount() + monthlyAudit.getAcceptedCount());
            totalTransactionCountDto.setRejectedCount(totalTransactionCountDto.getRejectedCount() + monthlyAudit.getRejectedCount());
            totalTransactionCountDto.setPendingCount(totalTransactionCountDto.getPendingCount() + monthlyAudit.getPendingCount());
            totalTransactionCountDto.setValidationErrorCount(totalTransactionCountDto.getValidationErrorCount() + monthlyAudit.getValidationErrorCount());
            totalTransactionCountDto.setCbpDownCount(totalTransactionCountDto.getCbpDownCount() + monthlyAudit.getCbpDownCount());
            totalTransactionCountDto.setTotalTransactions(totalTransactionCountDto.getTotalTransactions() + monthlyAudit.getTotalTransactions());
            totalTransactionCountDto.setMonth(monthlyAudit.getMonth());
        }
        totalTransactionCountDto.setAuditData(monthlyAudits.stream().map(this::convertToMonthlyAuditDto).toList());
        return totalTransactionCountDto;
    }

    private TotalTransactionCountDto<DailyAuditDTO> getAllTransactionsWithCountForDaily(List<DailyAudit> dailyAudits) {
        TotalTransactionCountDto<DailyAuditDTO> totalTransactionCountDto = new TotalTransactionCountDto<>();
        for (DailyAudit audit : dailyAudits) {
            if (audit != null) {
                totalTransactionCountDto.setAcceptedCount(totalTransactionCountDto.getAcceptedCount() + audit.getAcceptedCount());
                totalTransactionCountDto.setRejectedCount(totalTransactionCountDto.getRejectedCount() + audit.getRejectedCount());
                totalTransactionCountDto.setPendingCount(totalTransactionCountDto.getPendingCount() + audit.getPendingCount());
                totalTransactionCountDto.setValidationErrorCount(totalTransactionCountDto.getValidationErrorCount() + audit.getValidationErrorCount());
                totalTransactionCountDto.setCbpDownCount(totalTransactionCountDto.getCbpDownCount() + audit.getCbpDownCount());
                totalTransactionCountDto.setTotalTransactions(totalTransactionCountDto.getTotalTransactions() + audit.getTotalTransactions());
            }
        }
        totalTransactionCountDto.setAuditData(dailyAudits.stream().map(this::convertToMonthlyAuditDto).toList());
        return totalTransactionCountDto;
    }

    private TotalTransactionCountDto<YearlyAuditDto> calculateTotalTransactionCountsForYear(List<YearlyAudit> yearlyAudits) {
        TotalTransactionCountDto<YearlyAuditDto> totalTransactionCountDto = new TotalTransactionCountDto<>();
        for (YearlyAudit yearlyAudit : yearlyAudits) {
            totalTransactionCountDto.setAcceptedCount(totalTransactionCountDto.getAcceptedCount() + yearlyAudit.getAcceptedCount());
            totalTransactionCountDto.setRejectedCount(totalTransactionCountDto.getRejectedCount() + yearlyAudit.getRejectedCount());
            totalTransactionCountDto.setPendingCount(totalTransactionCountDto.getPendingCount() + yearlyAudit.getPendingCount());
            totalTransactionCountDto.setValidationErrorCount(totalTransactionCountDto.getValidationErrorCount() + yearlyAudit.getValidationErrorCount());
            totalTransactionCountDto.setCbpDownCount(totalTransactionCountDto.getCbpDownCount() + yearlyAudit.getCbpDownCount());
            totalTransactionCountDto.setTotalTransactions(totalTransactionCountDto.getTotalTransactions() + yearlyAudit.getTotalTransactions());
            totalTransactionCountDto.setYear(yearlyAudit.getYear());
        }
        totalTransactionCountDto.setAuditData(yearlyAudits.stream().map(this::convertYearlyAuditToDto).toList());
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

    public YearlyAuditDto convertYearlyAuditToDto(YearlyAudit yearlyAudit) {
        YearlyAuditDto yearlyAuditDto = new YearlyAuditDto();
        yearlyAuditDto.setId(yearlyAudit.getId());
        yearlyAuditDto.setUserId(yearlyAudit.getUserId());
        yearlyAuditDto.setYear(yearlyAudit.getYear());
        yearlyAuditDto.setAcceptedCount(yearlyAudit.getAcceptedCount());
        yearlyAuditDto.setRejectedCount(yearlyAudit.getRejectedCount());
        yearlyAuditDto.setPendingCount(yearlyAudit.getPendingCount());
        yearlyAuditDto.setValidationErrorCount(yearlyAudit.getValidationErrorCount());
        yearlyAuditDto.setCbpDownCount(yearlyAudit.getCbpDownCount());
        yearlyAuditDto.setTotalTransactions(yearlyAudit.getTotalTransactions());
        return yearlyAuditDto;
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

}
