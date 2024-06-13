package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class UtilMethods {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager entityManager;
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
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error in parsing date "+date);
        }
    }

    public synchronized Long getNumberOfRecords(String schemaName,String tableName){
        String sql="SELECT COUNT(*) FROM "+schemaName+"."+tableName+";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    };

    public synchronized Integer getCountOfPartitionTables(String schemaName, String tableNamePrefix) {
        String sql = "SELECT COUNT(*) " +
                "FROM pg_class " +
                "WHERE relname LIKE ? " +
                "AND relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = ?) " +
                "AND relkind = 'r'";
        return jdbcTemplate.queryForObject(sql, Integer.class, tableNamePrefix + "_%", schemaName);
    }

    public synchronized Long getLastIdInTheTable(String schemaName, String tableName){
        String sql="SELECT COALESCE(MAX(serial), 0) AS last_id FROM "+schemaName+"."+tableName+";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public synchronized Long getMinIdForPartition(String schemaName, String tableName, int i) {
        String sql = "SELECT MIN(serial) FROM " + schemaName + "." + tableName + "_" + i + ";";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    public synchronized Long getMaxIdForPartition(String schemaName, String tableName, int i) {
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
        if(refId.length()!=31){
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_REFERENCE_ID,"Length mismatch : Expected 31,Actual "+refId.length());
        }
        String schemaName=getSchemaName(refId);
        String tableName=getTableName(refId).toLowerCase();
        if(!isSchemaExist(schemaName) && !isTableExist(schemaName,tableName))
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_REFERENCE_ID);
        return List.of(schemaName,tableName);
    }
    public List<String> getNotificationEmailsByUserIdentifier(String userIdentifier) {
        Query query = entityManager.createNativeQuery("SELECT notification_emails FROM public._user WHERE unique_user_identifier = :userIdentifier");
        query.setParameter("userIdentifier", userIdentifier);
        List<String> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Collections.emptyList();
        } else {
            String notificationEmails = resultList.get(0);
            if (notificationEmails == null) {
                return Collections.emptyList();
            } else {
                return Arrays.asList(notificationEmails.split(","));
            }
        }
    }

    public String getEmailByUserIdentifier(String userIdentifier) {
        Query query = entityManager.createNativeQuery("SELECT email FROM public._user WHERE unique_user_identifier = :userIdentifier");
        query.setParameter("userIdentifier", userIdentifier);
        List<String> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return null; // or throw an exception
        } else {
            return resultList.get(0);
        }
    }

}
