package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.exception.InvalidReferenceIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
public class UtilMethods {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(new Date());
    }
    public Long getFormattedDateInLong(){
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = today.format(formatter);
        return Long.parseLong(formattedDate);
    }

    public String getFormattedDate(String date){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            throw new RuntimeException("Error in parsing");
        }
    }

    public Long getNumberOfRecords(String schemaName,String tableName){
        String sql="SELECT COUNT(*) FROM "+schemaName+"."+tableName+";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    };

    public Integer getCountOfPartitionTables(String schemaName, String tableNamePrefix) {
        String sql = "SELECT COUNT(*) " +
                "FROM pg_class " +
                "WHERE relname LIKE ? " +
                "AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = ?) " +
                "AND relkind = 'r'";
        return jdbcTemplate.queryForObject(sql, Integer.class, tableNamePrefix + "_%", schemaName);
    }

    public Long getLastIdInTheTable(String schemaName, String tableName){
        String sql="SELECT COALESCE(MAX(serial), 0) AS last_id FROM "+schemaName+"."+tableName+";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getMinIdForPartition(String schemaName, String tableName, int i) {
        String sql = "SELECT MIN(serial) FROM " + schemaName + "." + tableName + "_" + i + ";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public Long getMaxIdForPartition(String schemaName, String tableName, int i) {
        String sql="SELECT COALESCE(MAX(serial), 0) AS last_id FROM "+schemaName+"."+ tableName + "_" + i + ";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    public String getSchemaNameFromDate(String date){
        return "fdapn_"+getFormattedDate(date);
    }
    public String getSchemaName(String refId){
        String datePart = refId.substring(refId.length() - 16, refId.length() - 8);
        return "fdapn_"+datePart;
    }
    public String getTableName(String refNum){
        if (refNum.length() <= 6) {
            return "";
        }
        return "fdapn_"+refNum.substring(0, refNum.length() - 16);
    }
    public void deletePartitionTable(String schemaName, String tableName, Integer partition) {
        String sql = "DROP TABLE IF EXISTS " + schemaName + "." + tableName + "_" + partition;
        jdbcTemplate.execute(sql);
    }
    public boolean isSchemaExist(String schemaName){
        String query = "SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, schemaName));
    }
    public boolean isTableExist(String tableName,String schemaName){
        String query = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(query, Boolean.class, schemaName, tableName));
    }
    public List<String> validateRefId(String refId){
        if(refId.length()!=26){
            throw new InvalidReferenceIdException("Invalid Reference id");
        }
        String schemaName=getSchemaName(refId);
        String tableName=getTableName(refId);
        if(!isSchemaExist(schemaName) && !isTableExist(schemaName,tableName))
            throw new InvalidReferenceIdException("Invalid Reference id");
        return List.of(schemaName,tableName);
    }
}
