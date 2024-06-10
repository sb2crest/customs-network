package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.service.AWSS3Services;
import com.customs.network.fdapn.utils.CustomIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CBPServiceImpl {
    private final CustomIdGenerator idGenerator;
    private final AWSS3Services s3Services;

    public CBPServiceImpl(CustomIdGenerator idGenerator, AWSS3Services s3Services) {
        this.idGenerator = idGenerator;
        this.s3Services = s3Services;
    }

    public void hitCbp(String ediData,String refID) {
        Long sNo=idGenerator.extractIdFromRefId(refID);
        if(sNo%17==0){
            s3Services.saveCbpDownFiles(ediData,refID);
        }
    }
}
