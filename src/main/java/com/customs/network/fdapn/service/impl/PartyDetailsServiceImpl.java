package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.UserPartyInfoDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.UserPartyInfo;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.repository.UserPartyInfoRepository;
import com.customs.network.fdapn.service.PartyDetailsService;
import com.customs.network.fdapn.utils.JsonUtils;
import com.customs.network.fdapn.validations.objects.commodity.EntityDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.customs.network.fdapn.utils.CustomIdGenerator.generatePartyIdentifierId;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Service
@Slf4j
public class PartyDetailsServiceImpl implements PartyDetailsService {
    private final UserPartyInfoRepository userPartyInfoRepository;

    public PartyDetailsServiceImpl(UserPartyInfoRepository userPartyInfoRepository) {
        this.userPartyInfoRepository = userPartyInfoRepository;
    }

    Cache<String, UserPartyInfoDto> partyInfoCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Override
    public UserPartyInfoDto addParty(UserPartyInfoDto userPartyInfoDto) {
        UserPartyInfo userPartyInfo = getUserPartyInfo(userPartyInfoDto);
        try {
            userPartyInfo.setPartyIdentifierId(generatePartyIdentifierId());
            UserPartyInfo savedInfo = userPartyInfoRepository.save(userPartyInfo);
            String cacheKey = userPartyInfoDto.getUniqueUserIdentifier() + "-" + savedInfo.getPartyIdentifierId();
            cachePartyInfo(cacheKey, userPartyInfoDto);
            return getUserPartyInfoDto(savedInfo);
        } catch (Exception e) {
            log.error("Unable to save party information for user {}. Error: {}", userPartyInfoDto.getUniqueUserIdentifier(), e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.INTERNAL_SERVER_ERROR, "Unable to save party information. Please try again later.");
        }
    }

    @Override
    public String updatedParty(UserPartyInfoDto userPartyInfoDto) {
        String partyIdentifierId = userPartyInfoDto.getPartyIdentifierId();
        String uniqueUserIdentifier = userPartyInfoDto.getUniqueUserIdentifier();
        if (StringUtils.isBlank(partyIdentifierId)) {
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "party identifier id is required");
        }
        UserPartyInfo userPartyInfo = supplyUserPartyInfo(uniqueUserIdentifier, partyIdentifierId);
        userPartyInfo.setPartyInfo(userPartyInfoDto.getPartyInfo());
        try {
            userPartyInfoRepository.save(userPartyInfo);
            String cacheKey = uniqueUserIdentifier + "-" + partyIdentifierId;
            cachePartyInfo(cacheKey, userPartyInfoDto);
        } catch (Exception e) {
            log.error("Unable to update party information for user {}. Error: {}", userPartyInfoDto.getUniqueUserIdentifier(), e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.INTERNAL_SERVER_ERROR, "Unable to update party information. Please try again later.");
        }
        return "Successfully Updated UserPartyInfo";
    }

    @Override
    @Cacheable(value = "partyInfoCache", key = "#uniqueUserIdentifier + '_' + #partyIdentifierId")
    public UserPartyInfoDto getUserPartyInfo(String uniqueUserIdentifier, String partyIdentifierId) {
        log.info("Party information with id {} not found in the cache, Fetching from database", partyIdentifierId);
        UserPartyInfo userPartyInfo = supplyUserPartyInfo(uniqueUserIdentifier, partyIdentifierId);
        UserPartyInfoDto userPartyInfoDto = getUserPartyInfoDto(userPartyInfo);
        cachePartyInfo(uniqueUserIdentifier + '_' + partyIdentifierId, userPartyInfoDto);
        return userPartyInfoDto;
    }

    @Override
    @CacheEvict(value = "partyInfoCache", key = "#uniqueUserIdentifier + '_' + #partyIdentifierId")
    public String deletedParty(String uniqueUserIdentifier, String partyIdentifierId) {
        UserPartyInfo userPartyInfo = supplyUserPartyInfo(uniqueUserIdentifier, partyIdentifierId);
        try {
            userPartyInfoRepository.delete(userPartyInfo);
        } catch (Exception e) {
            log.error("Unable to delete party information for user {}. Error: {}", uniqueUserIdentifier, e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.INTERNAL_SERVER_ERROR, "Unable to delete party information. Please try again later.");
        }
        return String.format("Successfully deleted party information where uniqueUserIdentifier %s and partyIdentifierId %s", uniqueUserIdentifier, partyIdentifierId);
    }


    public List<EntityDetails> getMultiplePartyTypes(List<String> partyIdentifiers, String uniqueUserIdentifier, List<ValidationError> errors) {
        if (StringUtils.isNotEmpty(uniqueUserIdentifier)) {
            log.error("No uniqueUserIdentifier was specified for fetching party information");
            return Collections.emptyList();
        }
        if (partyIdentifiers == null || partyIdentifiers.isEmpty()) {
            log.error("No party identifiers were specified for user {}", uniqueUserIdentifier);
            return Collections.emptyList();
        }
        return fetchMultiplePartyInfo(partyIdentifiers, uniqueUserIdentifier, errors);
    }

    public List<EntityDetails> fetchMultiplePartyInfo(List<String> partyIdentifiers, String uniqueUserIdentifier, List<ValidationError> errors) {
        return partyIdentifiers.stream().
                filter(Objects::nonNull)
                .map(partyIdentifier -> {
                    JsonNode partyJsonData = null;
                    try {
                        UserPartyInfoDto data = getUserPartyInfo(uniqueUserIdentifier, partyIdentifier);
                        partyJsonData = data.getPartyInfo();
                    } catch (FdapnCustomExceptions e) {
                        errors.add(createValidationError("partyDetails", String.format("No party information found for partyIdentifierId %s and uniqueUserIdentifier %s", partyIdentifier, uniqueUserIdentifier), null));
                        log.error("Unable to fetch party information for user {} and partyIdentifierId {} Skipping. Error: {}", uniqueUserIdentifier, partyIdentifier, e.getMessage());
                    }
                    return partyJsonData;
                })
                .filter(Objects::nonNull)
                .map(JsonUtils::convertJsonNodeToEntityDetails).toList();
    }


    //helper methods----------------------------------------------------------------

    public void cachePartyInfo(String cacheKey, UserPartyInfoDto userPartyInfoDto) {
        partyInfoCache.put(cacheKey, userPartyInfoDto);
    }

    private UserPartyInfo supplyUserPartyInfo(String uniqueUserIdentifier, String partyIdentifierId) {
        return userPartyInfoRepository.findByUniqueUserIdentifierAndPartyIdentifierId(uniqueUserIdentifier, partyIdentifierId).
                orElseThrow(() -> new FdapnCustomExceptions(ErrorResCodes.RECORD_NOT_FOUND,
                        String.format("Not Data found for uniqueUserIdentifier %s and partyIdentifierId %s", uniqueUserIdentifier, partyIdentifierId)));
    }

    private UserPartyInfo getUserPartyInfo(UserPartyInfoDto userPartyInfoDto) {
        return UserPartyInfo.builder()
                .uniqueUserIdentifier(userPartyInfoDto.getUniqueUserIdentifier())
                .partyInfo(userPartyInfoDto.getPartyInfo())
                .build();
    }

    private UserPartyInfoDto getUserPartyInfoDto(UserPartyInfo userPartyInfo) {
        return UserPartyInfoDto.builder()
                .partyIdentifierId(String.valueOf(userPartyInfo.getPartyIdentifierId()))
                .uniqueUserIdentifier(userPartyInfo.getUniqueUserIdentifier())
                .partyInfo(userPartyInfo.getPartyInfo())
                .build();
    }

}
