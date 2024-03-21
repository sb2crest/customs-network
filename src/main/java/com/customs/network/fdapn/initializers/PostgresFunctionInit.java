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
    public void createWriteDailyAuditDataFunction() {
        String sqlFunction = "CREATE OR REPLACE FUNCTION write_daily_audit_data(date_param VARCHAR) " +
                "RETURNS VOID " +
                "AS $$ " +
                "DECLARE " +
                "    schema_name_var TEXT; " +
                "    table_name_var TEXT; " +
                "    user_id_var VARCHAR; " +
                "    audit_date DATE := date_param::DATE; " +
                "    total_success_var BIGINT := 0; " +
                "    total_failed_var BIGINT := 0; " +
                "    total_pending_var BIGINT := 0; " +
                "    total_cbp_down_var BIGINT := 0; " +
                "    total_transactions_var BIGINT := 0; " +
                "    dynamic_query TEXT; " +
                "BEGIN " +
                "    FOR table_name_var IN " +
                "        SELECT table_name " +
                "        FROM information_schema.tables " +
                "        WHERE table_schema = 'fdapn_' || to_char(audit_date, 'YYYYMMDD') " +
                "        AND table_name !~ '.*_[0-9]+$' " +
                "    LOOP " +
                "        EXECUTE 'SELECT substring($1 from ''fdapn_(.*)'')' INTO user_id_var USING table_name_var; " +
                "        dynamic_query := 'SELECT " +
                "                            COALESCE(SUM(CASE WHEN status = ''SUCCESS'' THEN 1 ELSE 0 END), 0), " +
                "                            COALESCE(SUM(CASE WHEN status = ''FAILED'' THEN 1 ELSE 0 END), 0), " +
                "                            COALESCE(SUM(CASE WHEN status = ''PENDING'' THEN 1 ELSE 0 END), 0), " +
                "                            COALESCE(SUM(CASE WHEN status = ''CBP DOWN'' THEN 1 ELSE 0 END), 0), " +
                "                            COUNT(*) " +
                "                          FROM fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var || " +
                "                          ' WHERE created_on::DATE = $1'; " +
                "        EXECUTE dynamic_query " +
                "        INTO total_success_var, total_failed_var, total_pending_var, total_cbp_down_var, total_transactions_var " +
                "        USING audit_date; " +
                "        BEGIN " +
                "            UPDATE public.daily_audit " +
                "            SET " +
                "                date = audit_date, " +
                "                failed = total_failed_var, " +
                "                pending = total_pending_var, " +
                "                success = total_success_var, " +
                "                cbp_down = total_cbp_down_var, " +
                "                total_transactions = total_transactions_var " +
                "            WHERE " +
                "                date = audit_date " +
                "                AND user_id = user_id_var; " +
                "            IF NOT FOUND THEN " +
                "                INSERT INTO public.daily_audit (date, failed, pending, success, cbp_down, total_transactions, user_id) " +
                "                VALUES (audit_date, total_failed_var, total_pending_var, total_success_var, total_cbp_down_var, total_transactions_var, user_id_var); " +
                "            END IF; " +
                "        EXCEPTION " +
                "            WHEN unique_violation THEN " +
                "                CONTINUE; " +
                "        END; " +
                "    END LOOP; " +
                "END; " +
                "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(sqlFunction);
    }
}
