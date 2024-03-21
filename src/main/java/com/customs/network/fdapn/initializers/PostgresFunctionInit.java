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
        String sqlFunction =  "CREATE OR REPLACE FUNCTION write_daily_audit_data(date_param VARCHAR)\n" +
                "RETURNS VOID AS $$\n" +
                "DECLARE\n" +
                "    schema_name_var TEXT;\n" +
                "    table_name_var TEXT;\n" +
                "    user_id_var VARCHAR;\n" +
                "    audit_date DATE := date_param::DATE;\n" +
                "    total_accepted_var BIGINT := 0;\n" +
                "    total_rejected_var BIGINT := 0;\n" +
                "    total_pending_var BIGINT := 0;\n" +
                "    total_cbp_down_var BIGINT := 0;\n" +
                "    total_transactions_var BIGINT := 0;\n" +
                "    dynamic_query TEXT;\n" +
                "BEGIN\n" +
                "    FOR table_name_var IN\n" +
                "        SELECT table_name\n" +
                "        FROM information_schema.tables\n" +
                "        WHERE table_schema = 'fdapn_' || to_char(audit_date, 'YYYYMMDD')\n" +
                "        AND table_name !~ '.*_[0-9]+$'\n" +
                "    LOOP\n" +
                "        EXECUTE 'SELECT user_id,\n" +
                "                        COALESCE(SUM(CASE WHEN status = ''ACCEPTED'' THEN 1 ELSE 0 END), 0),\n" +
                "                        COALESCE(SUM(CASE WHEN status = ''REJECTED'' THEN 1 ELSE 0 END), 0),\n" +
                "                        COALESCE(SUM(CASE WHEN status = ''PENDING'' THEN 1 ELSE 0 END), 0),\n" +
                "                        COALESCE(SUM(CASE WHEN status = ''CBP DOWN'' THEN 1 ELSE 0 END), 0),\n" +
                "                        COUNT(*)\n" +
                "                  FROM fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var ||\n" +
                "                  ' WHERE created_on::DATE = $1\n" +
                "                  GROUP BY user_id'\n" +
                "        INTO user_id_var, total_accepted_var, total_rejected_var, total_pending_var, total_cbp_down_var, total_transactions_var\n" +
                "        USING audit_date;\n" +
                "        UPDATE public.daily_audit\n" +
                "        SET\n" +
                "            date = audit_date,\n" +
                "            rejected = total_rejected_var,\n" +
                "            pending = total_pending_var,\n" +
                "            accepted = total_accepted_var,\n" +
                "            cbp_down = total_cbp_down_var,\n" +
                "            total_transactions = total_transactions_var\n" +
                "        WHERE\n" +
                "            date = audit_date\n" +
                "            AND user_id = user_id_var;\n" +
                "        IF NOT FOUND THEN\n" +
                "            INSERT INTO public.daily_audit (date, rejected, pending, accepted, cbp_down, total_transactions, user_id)\n" +
                "            VALUES (audit_date, total_rejected_var, total_pending_var, total_accepted_var, total_cbp_down_var, total_transactions_var, user_id_var);\n" +
                "        END IF;\n" +
                "    END LOOP;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(sqlFunction);
    }
}
