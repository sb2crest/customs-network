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
public class PortInfoUpdateScheduler {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PostgresFunctionInit postgresFunctionInit;
    @PostConstruct
    void init(){
        postgresFunctionInit.createWritePortInfoFunction();
    }
    @Scheduled(fixedRate = 30000)
    public void executeAuditFunction() {
        String dateParam = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        log.info("Scanning schema for port Info started");
        String sql = "SELECT update_port_info(?,NULL)";
        jdbcTemplate.execute(sql, (PreparedStatementCallback<Void>) ps -> {
            ps.setString(1, dateParam);
            ps.execute();
            return null;
        });
        log.info("Scanning schema completed and Port Info table updated");
    }

}
