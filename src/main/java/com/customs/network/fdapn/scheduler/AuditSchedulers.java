package com.customs.network.fdapn.scheduler;

import com.customs.network.fdapn.initializers.PostgresFunctionInit;
import com.customs.network.fdapn.service.AuditService;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AuditSchedulers {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostgresFunctionInit postgresFunctionInit;
    @Autowired
    AuditService auditService;
    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    void init() {
        postgresFunctionInit.createWriteDailyAuditDataFunction();
    }

    @Scheduled(fixedRate = 30000)
    public void executeDailyAuditFunction() {
        clearCacheEntriesForPeriod("today");
        clearCacheEntriesForPeriod("week");
        String functionName = "write_daily_audit_data";
        String dateParam = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        log.info("Scanning schema for audit started");
        jdbcTemplate.execute("SELECT " + functionName + "(?)", (PreparedStatementCallback<Void>) ps -> {
            ps.setString(1, dateParam);
            ps.execute();
            return null;
        });
        log.info("Scanning schema completed and Daily audit table updated");
    }

    @Scheduled(cron = "0 35 10 * * ?")
    public void monthlyAuditTableUpdateScheduler() throws ParseException {
        clearCacheEntriesForPeriod("month");
        log.info("Updating monthly audit table");
        auditService.auditAndUpdateMonthlyAuditTable();
        log.info("Updated Monthly table");
    }

    @Scheduled(cron = "0 0 11 1 * ?")
    public void yearlyAuditTableUpdateScheduler(){
        clearCacheEntriesForPeriod("year");
        log.info("Updating yearly audit table");
        auditService.auditAndUpdateYearlyAuditTable();
        log.info("Updated yearly table");
    }


    public void clearCacheEntriesForPeriod(String period) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("transactionsCache");
        assert caffeineCache != null;
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        Pattern pattern = Pattern.compile(".*_" + Pattern.quote(period) + "$");
        List<Object> keysToRemove = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : nativeCache.asMap().entrySet()) {
            String key = entry.getKey().toString();
            log.info("Key - {}", key);
            if (pattern.matcher(key).matches()) {
                log.info("Matching key - {}", key);
                keysToRemove.add(key);
            }
        }
        keysToRemove.forEach(key -> {
            log.info("Invalidating key - {}", key);
            nativeCache.invalidate(key);
        });
    }
}
