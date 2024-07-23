package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.UserPartyInfoDto;

public interface PartyDetailsService {
    UserPartyInfoDto addParty(UserPartyInfoDto userPartyInfoDto);
    String updatedParty(UserPartyInfoDto userPartyInfoDto);

    UserPartyInfoDto getUserPartyInfo(String uniqueUserIdentifier, String partyIdentifierId);

    String deletedParty(String uniqueUserIdentifier, String partyIdentifierId);
}
