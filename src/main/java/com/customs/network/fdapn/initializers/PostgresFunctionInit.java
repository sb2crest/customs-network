package com.customs.network.fdapn.initializers;

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
        String functionSql = "CREATE OR REPLACE FUNCTION fetch_data_by_status_and_date("
                + "fieldName VARCHAR, "
                + "p_value VARCHAR, "
                + "p_schemaNamePrefix VARCHAR, "
                + "p_startDate VARCHAR DEFAULT NULL, "
                + "p_endDate VARCHAR DEFAULT NULL, "
                + "p_userId VARCHAR DEFAULT NULL) "
                + "RETURNS TABLE ("
                + "serial BIGINT, "
                + "batch_id VARCHAR(255), "
                + "created_on TIMESTAMP WITHOUT TIME ZONE, "
                + "envelop_number VARCHAR(255), "
                + "reference_id VARCHAR(255), "
                + "request_json JSONB, "
                + "response_json JSONB, "
                + "status VARCHAR(255), "
                + "trace_id VARCHAR(255), "
                + "updated_on DATE, "
                + "unique_user_identifier VARCHAR(255)) "
                + "AS $$ "
                + "DECLARE "
                + "table_name_var TEXT; "
                + "schema_name_var TEXT; "
                + "loop_date DATE; "
                + "BEGIN "
                + "IF p_endDate IS NOT NULL AND p_startDate IS NULL THEN "
                + "p_startDate := p_endDate; "
                + "END IF; "
                + "IF p_startDate IS NULL OR p_endDate IS NULL THEN "
                + "RAISE EXCEPTION 'Both start date and end date must be provided.'; "
                + "END IF; "
                + "loop_date := p_startDate::DATE; "
                + "WHILE loop_date <= p_endDate::DATE LOOP "
                + "schema_name_var := p_schemaNamePrefix || '_' || to_char(loop_date, 'YYYYMMDD'); "
                + "IF p_userId IS NOT NULL THEN "
                + "table_name_var := p_schemaNamePrefix || '_' || p_userId; "
                + "IF EXISTS ( "
                + "SELECT 1 "
                + "FROM information_schema.tables "
                + "WHERE table_schema = schema_name_var "
                + "AND table_name = table_name_var "
                + ") THEN "
                + "RAISE NOTICE 'Querying table: %', table_name_var; "
                + "RETURN QUERY EXECUTE "
                + "'SELECT DISTINCT * FROM ' || schema_name_var || '.' || table_name_var || "
                + "CASE "
                + "WHEN fieldName IS NOT NULL AND p_value IS NOT NULL THEN "
                + "' WHERE ' || quote_ident(fieldName) || ' = $1' "
                + "ELSE "
                + "'' "
                + "END "
                + "USING p_value; "
                + "END IF; "
                + "ELSE "
                + "FOR table_name_var IN ( "
                + "SELECT table_name "
                + "FROM information_schema.tables "
                + "WHERE table_schema = schema_name_var "
                + "AND table_type = 'BASE TABLE' "
                + "AND table_name !~ '.*_[0-9]+$' "
                + ") "
                + "LOOP "
                + "RAISE NOTICE 'Querying table: %', table_name_var; "
                + "RETURN QUERY EXECUTE "
                + "'SELECT DISTINCT * FROM ' || schema_name_var || '.' || table_name_var || "
                + "CASE "
                + "WHEN fieldName IS NOT NULL AND p_value IS NOT NULL THEN "
                + "' WHERE ' || quote_ident(fieldName) || ' = $1' "
                + "ELSE "
                + "'' "
                + "END "
                + "USING p_value; "
                + "END LOOP; "
                + "END IF; "
                + "loop_date := loop_date + INTERVAL '1 day'; "
                + "END LOOP; "
                + "END; "
                + "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(functionSql);
    }
    public void createWriteDailyAuditDataFunction() {
        String sqlFunction = "CREATE OR REPLACE FUNCTION write_daily_audit_data(date_param VARCHAR)\n" +
                "RETURNS VOID AS\n" +
                "$$\n" +
                "DECLARE\n" +
                "    schema_name_var TEXT;\n" +
                "    table_name_var TEXT;\n" +
                "    user_id_var VARCHAR;\n" +
                "    audit_date DATE := date_param::DATE;\n" +
                "    total_accepted_var BIGINT := 0;\n" +
                "    total_rejected_var BIGINT := 0;\n" +
                "    total_pending_var BIGINT := 0;\n" +
                "    total_cbp_down_var BIGINT := 0;\n" +
                "    total_validation_error_var BIGINT := 0;\n" +
                "    total_transactions_var BIGINT := 0;\n" +
                "    dynamic_query TEXT;\n" +
                "BEGIN\n" +
                "    FOR table_name_var IN\n" +
                "        SELECT table_name\n" +
                "        FROM information_schema.tables\n" +
                "        WHERE table_schema = 'fdapn_' || to_char(audit_date, 'YYYYMMDD')\n" +
                "        AND table_name !~ '.*_[0-9]+$'\n" +
                "    LOOP\n" +
                "        EXECUTE 'SELECT \n" +
                "                      unique_user_identifier,\n" +
                "                      COALESCE(SUM(CASE WHEN status = ''ACCEPTED'' THEN 1 ELSE 0 END), 0),\n" +
                "                      COALESCE(SUM(CASE WHEN status = ''REJECTED'' THEN 1 ELSE 0 END), 0),\n" +
                "                      COALESCE(SUM(CASE WHEN status = ''PENDING'' THEN 1 ELSE 0 END), 0),\n" +
                "                      COALESCE(SUM(CASE WHEN status = ''CBP DOWN'' THEN 1 ELSE 0 END), 0),\n" +
                "                      COALESCE(SUM(CASE WHEN status = ''VALIDATION ERROR'' THEN 1 ELSE 0 END), 0),\n" +
                "                      COUNT(*)\n" +
                "                  FROM fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var ||\n" +
                "                  ' WHERE created_on::DATE = $1\n" +
                "                  GROUP BY unique_user_identifier'\n" +
                "        INTO user_id_var, total_accepted_var, total_rejected_var, total_pending_var, total_cbp_down_var, total_validation_error_var, total_transactions_var\n" +
                "        USING audit_date;\n" +
                "        \n" +
                "        UPDATE public.daily_audit\n" +
                "        SET\n" +
                "            date = audit_date,\n" +
                "            rejected = total_rejected_var,\n" +
                "            pending = total_pending_var,\n" +
                "            accepted = total_accepted_var,\n" +
                "            cbp_down = total_cbp_down_var,\n" +
                "            validation_error = total_validation_error_var,\n" +
                "            total_transactions = total_transactions_var\n" +
                "        WHERE\n" +
                "            date = audit_date\n" +
                "            AND unique_user_identifier = user_id_var;\n" +
                "        \n" +
                "        IF NOT FOUND THEN\n" +
                "            INSERT INTO public.daily_audit (date, rejected, pending, accepted, cbp_down, validation_error, total_transactions, unique_user_identifier)\n" +
                "            VALUES (audit_date, total_rejected_var, total_pending_var, total_accepted_var, total_cbp_down_var, total_validation_error_var, total_transactions_var, user_id_var);\n" +
                "        END IF;\n" +
                "    END LOOP;\n" +
                "END;\n" +
                "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(sqlFunction);
    }
    public void createWritePortInfoFunction() {
        String fillPortInfoFunction =
                "CREATE OR REPLACE FUNCTION update_port_info(date_param VARCHAR, user_id_param VARCHAR DEFAULT NULL) " +
                        "RETURNS VOID AS $$ " +
                        "DECLARE " +
                        "    schema_name_var TEXT; " +
                        "    table_name_var TEXT; " +
                        "    user_id_var VARCHAR; " +
                        "    audit_date DATE := date_param::DATE; " +
                        "    port_number_var INTEGER; " +
                        "    total_accepted_var BIGINT := 0; " +
                        "    total_rejected_var BIGINT := 0; " +
                        "    total_pending_var BIGINT := 0; " +
                        "    arrival_location VARCHAR; " +
                        "BEGIN " +
                        "    FOR table_name_var IN " +
                        "        SELECT table_name " +
                        "        FROM information_schema.tables " +
                        "        WHERE table_schema = 'fdapn_' || to_char(audit_date, 'YYYYMMDD') " +
                        "        AND table_name !~ '.*_[0-9]+$' " +
                        "        AND (user_id_param IS NULL OR table_name LIKE 'fdapn_' || user_id_param || '%') " +
                        "    LOOP " +
                        "        RAISE NOTICE 'Scanning table %', table_name_var; " +
                        "        BEGIN " +
                        "            EXECUTE 'SELECT user_id FROM ' || 'fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var || ' LIMIT 1' " +
                        "            INTO user_id_var; " +
                        "        EXCEPTION " +
                        "            WHEN others THEN " +
                        "                RAISE NOTICE 'Error getting user ID from table %: %', table_name_var, SQLERRM; " +
                        "                CONTINUE; " +
                        "        END; " +
                        "        IF user_id_param IS NOT NULL AND user_id_param <> user_id_var THEN " +
                        "            CONTINUE; " +
                        "        END IF; " +
                        "        FOR arrival_location IN " +
                        "            EXECUTE 'SELECT DISTINCT request_json->>''arrivalLocation'' FROM ' || 'fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var " +
                        "        LOOP " +
                        "            BEGIN " +
                        "                port_number_var := arrival_location::INTEGER; " +
                        "            EXCEPTION " +
                        "                WHEN others THEN " +
                        "                    RAISE NOTICE 'Error converting arrival_location to INTEGER for table %: %', table_name_var, SQLERRM; " +
                        "                    CONTINUE; " +
                        "            END; " +
                        "            BEGIN " +
                        "                EXECUTE 'SELECT " +
                        "                                COALESCE(SUM(CASE WHEN status = ''ACCEPTED'' THEN 1 ELSE 0 END), 0) AS total_accepted, " +
                        "                                COALESCE(SUM(CASE WHEN status = ''REJECTED'' THEN 1 ELSE 0 END), 0) AS total_rejected, " +
                        "                                COALESCE(SUM(CASE WHEN status = ''PENDING'' THEN 1 ELSE 0 END), 0) AS total_pending " +
                        "                          FROM ' || 'fdapn_' || to_char(audit_date, 'YYYYMMDD') || '.' || table_name_var || " +
                        "                          ' WHERE (request_json->>''arrivalLocation'')::INTEGER = $1' " +
                        "                INTO total_accepted_var, total_rejected_var, total_pending_var " +
                        "                USING port_number_var; " +
                        "            EXCEPTION " +
                        "                WHEN others THEN " +
                        "                    RAISE NOTICE 'Error calculating status counts for table %: %', table_name_var, SQLERRM; " +
                        "                    CONTINUE; " +
                        "            END; " +
                        "            BEGIN " +
                        "                UPDATE public.port_info " +
                        "                SET " +
                        "                    accepted_count = total_accepted_var, " +
                        "                    date = audit_date, " +
                        "                    pending_count = total_pending_var, " +
                        "                    rejected_count = total_rejected_var, " +
                        "                    total_count = total_accepted_var + total_rejected_var + total_pending_var " +
                        "                WHERE " +
                        "                    date = audit_date " +
                        "                    AND user_id = user_id_var " +
                        "                    AND port_number = port_number_var; " +
                        "                IF NOT FOUND THEN " +
                        "                    INSERT INTO public.port_info (accepted_count, date, pending_count, port_number, rejected_count, total_count, user_id) " +
                        "                    VALUES (total_accepted_var, audit_date, total_pending_var, port_number_var, total_rejected_var, total_accepted_var + total_rejected_var + total_pending_var, user_id_var); " +
                        "                END IF; " +
                        "            EXCEPTION " +
                        "                WHEN others THEN " +
                        "                    RAISE NOTICE 'Error updating or inserting into port_info table for table %: %', table_name_var, SQLERRM; " +
                        "            END; " +
                        "        END LOOP; " +
                        "    END LOOP; " +
                        "END; " +
                        "$$ LANGUAGE plpgsql;";

        jdbcTemplate.execute(fillPortInfoFunction);
    }

}
