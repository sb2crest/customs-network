package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.initializers.PostgresFunctionInit;
import com.customs.network.fdapn.model.TransactionInfo;
import com.customs.network.fdapn.repository.TransactionManagerRepo;
import com.customs.network.fdapn.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
@Service
@Slf4j
public class TransactionManagerServiceImpl implements TransactionManagerRepo {
    private final JdbcTemplate jdbcTemplate;
    private final CustomIdGenerator idGenerator;
    private final UtilMethods utilMethods;

    private final ObjectMapper objectMapper;

    private final PostgresFunctionInit postgresFunctionInit;
    @Value("${partitionSize}")
    private Integer max;

    public TransactionManagerServiceImpl(JdbcTemplate jdbcTemplate,
                                         CustomIdGenerator idGenerator,
                                         UtilMethods utilMethods,
                                         ObjectMapper objectMapper,
                                         PostgresFunctionInit postgresFunctionInit) {
        this.jdbcTemplate = jdbcTemplate;
        this.idGenerator = idGenerator;
        this.utilMethods = utilMethods;
        this.objectMapper = objectMapper;
        this.postgresFunctionInit = postgresFunctionInit;
    }

    @PostConstruct
    public void init() {
        postgresFunctionInit.scanSchemaFunctionInit();
    }

    @Override
    public TransactionInfo saveTransaction(TransactionInfo request) {
        String schema = "fdapn_" + utilMethods.getFormattedDate();
        String userIdLowerCase = request.getUniqueUserIdentifier().toLowerCase();
        String tableName = "fdapn_" + userIdLowerCase;
        if (!isTableExist(schema, tableName))
            createTable(schema, tableName);
        Long numberOfRecords = utilMethods.getNumberOfRecords(schema, tableName);
        Long lastId = utilMethods.getLastIdInTheTable(schema, tableName);
        int newMax = (lastId > numberOfRecords) ? (int) Math.ceil((double) lastId / max) * max : (int) Math.ceil((double) numberOfRecords / max) * max;
        String refId = idGenerator.generate(request.getUniqueUserIdentifier(), lastId);
        request.setReferenceId(refId);
        request.setSlNo(idGenerator.extractIdFromRefId(refId));
        if ((numberOfRecords >= newMax && numberOfRecords != 0) || (lastId == newMax && lastId > numberOfRecords)) {
            Long missingRecords = (lastId == newMax) ? lastId - numberOfRecords : 0;
            createPartitionTable(schema, tableName, numberOfRecords + missingRecords);
        }
        return saveToTransactionTable(request, schema, tableName);
    }

   @Override
    public List<TransactionInfo> saveTransaction(List<TransactionInfo> requestList) throws BatchInsertionException {
        long start = System.currentTimeMillis();
        String schema = "fdapn_" + utilMethods.getFormattedDate();
        String userIdLowerCase = requestList.get(0).getUniqueUserIdentifier().toLowerCase();
        String tableName = "fdapn_" + userIdLowerCase;
        if (!isTableExist(schema, tableName))
            createTable(schema, tableName);

        final Long[] lastId = {utilMethods.getLastIdInTheTable(schema, tableName)};
        final Long[] numberOfRecords = {utilMethods.getLastIdInTheTable(schema, tableName)};
        List<TransactionInfo> list = requestList.stream()
                .filter(Objects::nonNull)
                .peek(request -> {
                    int newMax = (lastId[0] > numberOfRecords[0]) ? (int) Math.ceil((double) lastId[0] / max) * max : (int) Math.ceil((double) numberOfRecords[0] / max) * max;
                    String refId = idGenerator.generate(request.getUniqueUserIdentifier(), lastId[0]);
                    lastId[0]++;
                    request.setReferenceId(refId);
                    request.setSlNo(idGenerator.extractIdFromRefId(refId));
                    if ((numberOfRecords[0] >= newMax && numberOfRecords[0] != 0) || (lastId[0] == newMax && lastId[0] > numberOfRecords[0])) {
                        Long missingRecords = (lastId[0] == newMax) ? lastId[0] - numberOfRecords[0] : 0;
                        createPartitionTable(schema, tableName, numberOfRecords[0] + missingRecords);
                    }
                    numberOfRecords[0] = lastId[0];
                }).toList();
       long end = System.currentTimeMillis();
        log.info("Time to assign the reference id to the batch -> {} ",(end-start)/1000);
        return saveToTransactionTable(list, schema, tableName);
    }


    private boolean isTableExist(String schema, String tableName) {
        String sql = "SELECT EXISTS (" +
                "SELECT 1 " +
                "FROM information_schema.tables " +
                "WHERE table_schema = ? AND table_name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, schema, tableName));
    }

    private synchronized void createTable(String schemaName, String tableName) {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS " + schemaName + "." + tableName + " ("
                    + "serial BIGINT PRIMARY KEY,"
                    + "batch_id character varying(255) COLLATE pg_catalog.\"default\" NOT NULL,"
                    + "created_on timestamp,"
                    + "envelop_number character varying(255) COLLATE pg_catalog.\"default\","
                    + "reference_id character varying(255) COLLATE pg_catalog.\"default\","
                    + "request_json jsonb,"
                    + "response_json jsonb,"
                    + "status character varying(255) COLLATE pg_catalog.\"default\","
                    + "trace_id character varying(255) COLLATE pg_catalog.\"default\","
                    + "updated_on date,"
                    + "unique_user_identifier character varying(255) COLLATE pg_catalog.\"default\""
                    + ") PARTITION BY RANGE (serial)";

            jdbcTemplate.execute(sql);
            String firstPartitionTable = "CREATE TABLE " +
                    schemaName + "." + tableName + "_1 PARTITION OF " +
                    schemaName + "." +
                    tableName + " FOR VALUES FROM (1) TO (" + (max + 1) + ")";
            jdbcTemplate.execute(firstPartitionTable);
        } catch (DataAccessException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.SOMETHING_WENT_WRONG, "Error creating transaction table for  : " + e);
        }

    }

    private synchronized void createPartitionTable(String schemaName, String tableName, Long numberOfRecords) {
        try {
            Integer partitions = utilMethods.getCountOfPartitionTables(schemaName, tableName);
            Long noOfRecords = utilMethods.getLastIdInTheTable(schemaName, tableName + "_" + partitions);
            if (noOfRecords == 0) {
                utilMethods.deletePartitionTable(schemaName, tableName, partitions);
                partitions = utilMethods.getCountOfPartitionTables(schemaName, tableName);
            }
            String sql = "CREATE TABLE " +
                    schemaName + "." + tableName + "_" + (partitions + 1) + " PARTITION OF " +
                    schemaName + "." + tableName + " FOR VALUES FROM (" + (numberOfRecords + 1) + ") TO (" + (numberOfRecords + max + 1) + ")";
            jdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.SOMETHING_WENT_WRONG, "Error creating partition table : " + e);
        }
    }

    private synchronized TransactionInfo saveToTransactionTable(TransactionInfo request, String schemaName, String tableName) {
        try {
            String insertSql = "INSERT INTO " + schemaName + "." + tableName + " (serial, batch_id, created_on, envelop_number, reference_id, request_json, response_json, status, trace_id, updated_on, unique_user_identifier) "
                    + "VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb),CAST(? AS jsonb), ?, ?, ?, ?)";
            Date createdOnDate = request.getCreatedOn();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(createdOnDate);
            calendar.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));

            Timestamp createdOnTimestamp = new Timestamp(calendar.getTime().getTime());
            jdbcTemplate.update(
                    insertSql,
                    request.getSlNo(),
                    request.getBatchId(),
                    createdOnTimestamp,
                    request.getEnvelopNumber(),
                    request.getReferenceId(),
                    request.getRequestJson().toString(),
                    request.getResponseJson().toString(),
                    request.getStatus(),
                    request.getTraceId(),
                    request.getUpdatedOn(),
                    request.getUniqueUserIdentifier()
            );
        } catch (DuplicateKeyException keyException) {
            log.warn("Duplicate key error occurred for key {}. Retrying with a new key...", request.getSlNo());
        }
        return request;
    }

    private synchronized List<TransactionInfo> saveToTransactionTable(List<TransactionInfo> requests, String schemaName, String tableName) throws BatchInsertionException {
       long start = System.currentTimeMillis();
        try {
            String insertSql = "INSERT INTO " + schemaName + "." + tableName + " (serial, batch_id, created_on, envelop_number, reference_id, request_json, response_json, status, trace_id, updated_on, unique_user_identifier) "
                    + "VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            for (TransactionInfo request : requests) {
                Date createdOnDate = request.getCreatedOn();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(createdOnDate);
                calendar.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));

                Timestamp createdOnTimestamp = new Timestamp(calendar.getTime().getTime());

                batchArgs.add(new Object[]{
                        request.getSlNo(),
                        request.getBatchId(),
                        createdOnTimestamp,
                        request.getEnvelopNumber(),
                        request.getReferenceId(),
                        request.getRequestJson().toString(),
                        request.getResponseJson().toString(),
                        request.getStatus(),
                        request.getTraceId(),
                        request.getUpdatedOn(),
                        request.getUniqueUserIdentifier()
                });
            }

            jdbcTemplate.batchUpdate(insertSql, batchArgs);
        } catch (DuplicateKeyException keyException) {
            log.warn("Duplicate key error occurred. Some records may not be inserted.", keyException);
            throw new BatchInsertionException("Duplicate key error occurred", keyException);

        }
        long end = System.currentTimeMillis();
        log.info("Time taken to save the batch in the table ->{}", (end - start) / 1000.0);
        return requests;
    }


    @Override
    public TransactionInfo fetchTransaction(String refId) {
        List<String> location = utilMethods.validateRefId(refId);
        String schemaName = location.get(0);
        String tableName = location.get(1);
        Long slNumber = idGenerator.extractIdFromRefId(refId);
        Integer partitions = utilMethods.getCountOfPartitionTables(schemaName, tableName);
        int left = 1;
        int right = partitions;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Long minId = utilMethods.getMinIdForPartition(schemaName, tableName, mid);
            Long maxId = utilMethods.getMaxIdForPartition(schemaName, tableName, mid);
            if (slNumber >= minId && slNumber <= maxId) {
                String position = schemaName + "." + tableName + "_" + mid;
                return fetchTask(position, slNumber);
            } else if (slNumber < minId) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        return null;
    }

    @Override
    public void changeTransactionStatus(String refId, String newStatus) {
        List<String> location = utilMethods.validateRefId(refId);
        String schemaName = location.get(0);
        String tableName = location.get(1);
        Long slNumber = idGenerator.extractIdFromRefId(refId);
        Integer partitions = utilMethods.getCountOfPartitionTables(schemaName, tableName);
        int left = 1;
        int right = partitions;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            Long minId = utilMethods.getMinIdForPartition(schemaName, tableName, mid);
            Long maxId = utilMethods.getMaxIdForPartition(schemaName, tableName, mid);
            if (slNumber >= minId && slNumber <= maxId) {
                String position = schemaName + "." + tableName + "_" + mid;
                updateTaskStatus(position, slNumber, newStatus);
                return;
            } else if (slNumber < minId) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

    }

    private void updateTaskStatus(String position, Long slNumber, String newStatus) {
        try {
            String sql = "UPDATE " + position + " SET status = ? WHERE serial = ?";
            Object[] args = {newStatus, slNumber};
            int updatedRows = jdbcTemplate.update(sql, args);
            log.info("'{}' row is updated.Changed status to '{}' in the table '{}' for serial number '{}'", updatedRows, newStatus, position, slNumber);
        } catch (DataAccessException e) {
            log.error("Failed to update the status '{}' in table '{}' for serial number '{}'", newStatus, position, slNumber);
        }
    }

    private TransactionInfo fetchTask(String position, Long slNumber) {
        String sql = "SELECT * FROM " + position + " WHERE serial = ?";
        Object[] args = {slNumber};
        List<TransactionInfo> results = jdbcTemplate.query(sql, new ExtractTransactions(objectMapper), args);
        assert results != null;
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public PageDTO<TransactionInfo> fetchTransactionPages(FilterCriteriaDTO filterRequest) {
        try {
            String schemaName = utilMethods.getSchemaNameFromDate(DateUtils.formatterDate(filterRequest.getCreatedOn()));
            String tableName = "fdapn_" + filterRequest.getUserId();
            int partitionSize = max;
            Long totalRecords = utilMethods.getNumberOfRecords(schemaName, tableName);
            Long lastId = utilMethods.getLastIdInTheTable(schemaName, tableName);
            long missingRecords = lastId - totalRecords;
            if (missingRecords != 0) {
                totalRecords += missingRecords;
            }
            int startRecord = (filterRequest.getPage() - 1) * filterRequest.getSize() + 1;
            int endRecord = startRecord + filterRequest.getSize() - 1;
            int totalPartitions = (int) ((totalRecords - 1) / partitionSize + 1);
            int startPartition = (startRecord - 1) / partitionSize + 1;
            int endPartition = Math.min((endRecord - 1) / partitionSize + 1, totalPartitions);
            List<TransactionInfo> results = new LinkedList<>();
            for (int partition = endPartition; partition >= startPartition; partition--) {
                int partitionStartRecord = (partition - 1) * partitionSize + 1;
                int partitionEndRecord = partition * partitionSize;
                int offSet = 0;
                int recordsToFetch = 0;

                if (partitionStartRecord <= endRecord && partitionEndRecord >= startRecord) {
                    offSet = Math.max(startRecord - partitionStartRecord, 0);
                    recordsToFetch = Math.min(partitionEndRecord, endRecord) - Math.max(partitionStartRecord, startRecord) + 1;
                }

                if (recordsToFetch > 0) {
                    String partitionTableName = tableName + "_" + partition;
                    String sql = "SELECT * FROM " + schemaName + "." + partitionTableName + " ORDER BY serial desc LIMIT ? OFFSET ?";
                    Object[] args = {recordsToFetch, offSet};
                    List<TransactionInfo> partitionResults = jdbcTemplate.query(sql, new ExtractTransactions(objectMapper), args);
                    assert partitionResults != null;
                    results.addAll(partitionResults);
                    if (results.size() >= filterRequest.getSize()) {
                        break;
                    }
                }
            }
            PageDTO<TransactionInfo> pageDTO = new PageDTO<>();
            pageDTO.setPageSize(results.size());
            pageDTO.setPage(filterRequest.getPage());
            pageDTO.setTotalRecords(totalRecords - missingRecords);
            pageDTO.setData(results);
            return pageDTO;
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.SOMETHING_WENT_WRONG, "Error while fetching transaction pages\n " + e);
        }
    }


    @Override
    public PageDTO<TransactionInfo> fetchByFilter(FilterCriteriaDTO filterRequest) {
        try {
            if (StringUtils.isBlank(filterRequest.getStatus())) {
                return fetchTransactionPages(filterRequest);
            }

            String schemaName = utilMethods.getSchemaNameFromDate(DateUtils.formatterDate(filterRequest.getCreatedOn()));
            String tableName = "fdapn_" + filterRequest.getUserId();
            int offSet = (filterRequest.getPage() - 1) * filterRequest.getSize();

            String numberOfRecordsSql = "SELECT COUNT(*) FROM " + schemaName + "." + tableName + " WHERE status = ?;";
            String sql = "SELECT * FROM " + schemaName + "." + tableName + " WHERE status = ? ORDER BY serial DESC LIMIT ? OFFSET ?;";
            Object[] args = {filterRequest.getStatus(), filterRequest.getSize(), offSet};

            Long totalRecords = jdbcTemplate.queryForObject(numberOfRecordsSql, Long.class, filterRequest.getStatus());
            List<TransactionInfo> records = jdbcTemplate.query(sql, new ExtractTransactions(objectMapper), args);

            PageDTO<TransactionInfo> pageDTO = new PageDTO<>();
            pageDTO.setPageSize(records != null ? records.size() : 0);
            pageDTO.setPage(filterRequest.getPage());
            pageDTO.setTotalRecords(totalRecords);
            pageDTO.setData(records);

            return pageDTO;
        } catch (Exception e) {
            log.error("Exception while fetching records for user with id {} or similar. Cause :- {} ",
                    filterRequest.getUserId(),e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.SOMETHING_WENT_WRONG,
                    "Error fetching records by filter.Check whether the filter criteria is valid and try again");
        }
    }

    @Override
    public PageDTO<TransactionInfo> scanSchemaByColValue(String fieldName, String value, String startDate, String endDate, String userId, int page, int size) {
        if (!DateUtils.isValidDate(endDate))
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "Invalid End date(Expected format : yyyy-MM-dd , Provided : " + endDate + " )");
        if (startDate != null && !DateUtils.isValidDateFormat(startDate))
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "Invalid End date(Expected format : yyyy-MM-dd , Provided : " + startDate + " )");
        String query = "SELECT * FROM fetch_data_by_status_and_date(?, ?, ?, ?, ?, ?) " +
                "ORDER BY created_on DESC " +
                "LIMIT ? OFFSET ?";
        String countQuery = "SELECT COUNT(*) FROM fetch_data_by_status_and_date(?, ?, ?, ?, ?, ?)";
        String schemaPrefix = "fdapn";
        int offSet = (page - 1) * size;
        Object[] args = {fieldName, value, schemaPrefix, startDate, endDate, userId, size, offSet};
        Object[] argsForCount = {fieldName, value, schemaPrefix, startDate, endDate, userId};
        List<TransactionInfo> customsFdaPnSubmitListSubmit = jdbcTemplate.query(query, new ExtractTransactions(objectMapper), args);
        Long totalRecords = jdbcTemplate.queryForObject(countQuery, Long.class, argsForCount);
        PageDTO<TransactionInfo> pageDTO = new PageDTO<>();
        pageDTO.setPageSize(customsFdaPnSubmitListSubmit != null ? customsFdaPnSubmitListSubmit.size() : 0);
        pageDTO.setPage(page);
        pageDTO.setTotalRecords(totalRecords);
        pageDTO.setData(customsFdaPnSubmitListSubmit);
        return pageDTO;
    }

}
