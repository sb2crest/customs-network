package com.customs.network.fdapn.service;

import java.util.List;

public interface AWSS3Services {
    void saveCbpDownFiles(String ediContent, String refID);

    List<String> getTextFilesInFolder(String folderKey);

    List<String> getFoldersInBucket(String bucketName);
}
