package com.customs.network.fdapn.scheduler;

import com.customs.network.fdapn.initializers.PostgresFunctionInit;
import com.customs.network.fdapn.service.AuditService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Component
@Slf4j
public class AuditSchedulers {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostgresFunctionInit postgresFunctionInit;
    @Autowired
    AuditService auditService;
    @PostConstruct
    void init(){
        postgresFunctionInit.createWriteDailyAuditDataFunction();
    }
    @Scheduled(fixedRate = 30000)
    public void executeDailyAuditFunction() {
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
        log.info("Updating monthly audit table");
        auditService.auditAndUpdateMonthlyAuditTable();
        log.info("Updated Monthly table");
    }

    @Scheduled(cron = "0 0 11 1 * ?")
    public void yearlyAuditTableUpdateScheduler() throws ParseException {
        log.info("Updating yearly audit table");
        auditService.auditAndUpdateYearlyAuditTable();
        log.info("Updated yearly table");
    }

}
