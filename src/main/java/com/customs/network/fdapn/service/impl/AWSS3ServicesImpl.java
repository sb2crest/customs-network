package com.customs.network.fdapn.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.customs.network.fdapn.model.MessageCode;
import com.customs.network.fdapn.repository.TransactionRepository;
import com.customs.network.fdapn.service.AWSS3Services;
import com.customs.network.fdapn.utils.CustomIdGenerator;
import com.customs.network.fdapn.utils.UtilMethods;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class AWSS3ServicesImpl implements AWSS3Services {
    private final TransactionRepository transactionRepository;
    private final CustomIdGenerator idGenerator;
    private final UtilMethods utilMethods;
    private final AmazonS3 s3Client;

    public AWSS3ServicesImpl(TransactionRepository transactionRepository, CustomIdGenerator idGenerator, UtilMethods utilMethods, AmazonS3 s3Client) {
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
        this.utilMethods = utilMethods;
        this.s3Client = s3Client;
    }

    @Override
    public void saveCbpDownFiles(String ediContent, String refID) {
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String userId = idGenerator.extractUserIdFromRefId(refID);
        String folderKey = currentDate + "/" + "fdapn_" + userId + "/";
        String key = folderKey + refID + ".txt" ;
        transactionRepository.changeTransactionStatus(refID, MessageCode.CBP_DOWN.getStatus());
        log.info("Changed status to CBP DOWN for ref-> {}" , refID);
        try {
            byte[] contentBytes = ediContent.getBytes();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentBytes.length);
            s3Client.putObject(new PutObjectRequest("fdapn-submit-cbp-down-records", key, new ByteArrayInputStream(contentBytes), metadata));
            log.info("EDI content saved to S3 bucket: {}/{}", "fdapn-submit-cbp-down-records", key);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error occurred while saving EDI content to S3: " + e.getMessage());
        }
    }
    @Override
    public List<String> getTextFilesInFolder(String folderKey) {
        if(StringUtils.isNotBlank(folderKey)){
            folderKey = utilMethods.getFormattedDate(folderKey);
        }else {
            folderKey = utilMethods.getFormattedDate();
        }
        List<String> textFiles = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName("fdapn-submit-cbp-down-records")
                .withPrefix(folderKey);

        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(request);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                String key = objectSummary.getKey();
                if (key.endsWith(".txt")) {
                    textFiles.add(key);
                }
            }
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        textFiles.forEach(txt->{
            String refId  = txt.substring(txt.lastIndexOf("/") + 1, txt.lastIndexOf("."));
            transactionRepository.changeTransactionStatus(refId, MessageCode.SUCCESS_SUBMIT.getStatus());
            s3Client.deleteObject(new DeleteObjectRequest("fdapn-submit-cbp-down-records", txt));
        });
        return textFiles;
    }
    @Override
    public List<String> getFoldersInBucket(String bucketName) {
        List<String> folderKeys = new ArrayList<>();
        ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withDelimiter("/");
        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(request);
            folderKeys.addAll(result.getCommonPrefixes());
            request.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated());
        return folderKeys;
    }
}
