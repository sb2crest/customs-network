package com.customs.network.fdapn.scheduler;

import com.customs.network.fdapn.initializers.PostgresFunctionInit;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Component
@Slf4j
public class AuditSchedulers {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostgresFunctionInit postgresFunctionInit;
    @PostConstruct
    void init(){
        postgresFunctionInit.createWriteDailyAuditDataFunction();
    }
    @Scheduled(fixedRate = 30000)
    public void executeAuditFunction() {
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
}
