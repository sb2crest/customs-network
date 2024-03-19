package com.customs.network.fdapn.initializers;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresFunctionInit {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostgresFunctionInit(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void scanSchemaFunctionInit(){
        String functionSql = "CREATE OR REPLACE FUNCTION fetch_data_by_status_and_date(" +
                "fieldName VARCHAR," +
                "p_value VARCHAR," +
                "p_schemaNamePrefix VARCHAR," +
                "p_startDate VARCHAR DEFAULT NULL," +
                "p_endDate VARCHAR DEFAULT NULL," +
                "p_userId VARCHAR DEFAULT NULL" +
                ")" +
                "RETURNS TABLE (" +
                "serial BIGINT," +
                "batch_id VARCHAR(255)," +
                "account_id VARCHAR(255)," +
                "created_on TIMESTAMP WITHOUT TIME ZONE," +
                "envelop_number VARCHAR(255)," +
                "reference_id VARCHAR(255)," +
                "request_json JSONB," +
                "response_json JSONB," +
                "status VARCHAR(255)," +
                "trace_id VARCHAR(255)," +
                "updated_on DATE," +
                "user_id VARCHAR(255)" +
                ")" +
                "AS $$ " +
                "DECLARE " +
                "table_name_var TEXT; " +
                "schema_name_var TEXT; " +
                "loop_date DATE; " +
                "BEGIN " +
                "IF p_endDate IS NOT NULL AND p_startDate IS NULL THEN " +
                "p_startDate := p_endDate; " +
                "END IF; " +
                "IF p_startDate IS NULL OR p_endDate IS NULL THEN " +
                "RAISE EXCEPTION 'Both start date and end date must be provided.'; " +
                "END IF; " +
                "loop_date := p_startDate::DATE; " +
                "WHILE loop_date <= p_endDate::DATE LOOP " +
                "schema_name_var := p_schemaNamePrefix || '_' || to_char(loop_date, 'YYYYMMDD'); " +
                "IF p_userId IS NOT NULL THEN " +
                "table_name_var := p_schemaNamePrefix || '_' || p_userId; " +
                "IF EXISTS ( " +
                "SELECT 1 " +
                "FROM information_schema.tables " +
                "WHERE table_schema = schema_name_var " +
                "AND table_name = table_name_var " +
                ") THEN " +
                "RAISE NOTICE 'Querying table: %', table_name_var; " +
                "RETURN QUERY EXECUTE " +
                "'SELECT DISTINCT * FROM ' || schema_name_var || '.' || table_name_var || " +
                "' WHERE ' || quote_ident(fieldName) || ' = $1' " +
                "USING p_value; " +
                "END IF; " +
                "ELSE " +
                "FOR table_name_var IN ( " +
                "SELECT table_name " +
                "FROM information_schema.tables " +
                "WHERE table_schema = schema_name_var " +
                "AND table_type = 'BASE TABLE' " +
                "AND table_name !~ '.*_[0-9]+$' " +
                ") " +
                "LOOP " +
                "RAISE NOTICE 'Querying table: %', table_name_var; " +
                "RETURN QUERY EXECUTE " +
                "'SELECT DISTINCT * FROM ' || schema_name_var || '.' || table_name_var || " +
                "' WHERE ' || quote_ident(fieldName) || ' = $1' " +
                "USING p_value; " +
                "END LOOP; " +
                "END IF; " +
                "loop_date := loop_date + INTERVAL '1 day'; " +
                "END LOOP; " +
                "END; " +
                "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(functionSql);
    }
}
