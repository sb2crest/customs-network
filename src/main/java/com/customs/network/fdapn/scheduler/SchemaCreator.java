package com.customs.network.fdapn.scheduler;

import com.customs.network.fdapn.utils.UtilMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchemaCreator {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UtilMethods utilMethods;

    @Scheduled(fixedRate = 30000)
    public void createSchema() {
        String schemaName ="fdapn_"+utilMethods.getFormattedDate();
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
    }


}
