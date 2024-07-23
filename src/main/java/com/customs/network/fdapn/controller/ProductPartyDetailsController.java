package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.UserPartyInfoDto;
import com.customs.network.fdapn.service.PartyDetailsService;
import org.springframework.web.bind.annotation.*;

import static com.customs.network.fdapn.utils.ObjectValidations.validateUserPartyInfoDto;

@RestController
@RequestMapping("/party-info")
public class ProductPartyDetailsController {
    private final PartyDetailsService partyDetailsService;

    public ProductPartyDetailsController(PartyDetailsService partyDetailsService) {
        this.partyDetailsService = partyDetailsService;
    }

    @PostMapping("/add-new")
    public UserPartyInfoDto addNewPartyDetails(@RequestBody UserPartyInfoDto userPartyInfoDto) {
        validateUserPartyInfoDto(userPartyInfoDto);
        return partyDetailsService.addParty(userPartyInfoDto);
    }

    @PostMapping("/update-existing")
    public String updatePartyDetails(@RequestBody UserPartyInfoDto userPartyInfoDto) {
        validateUserPartyInfoDto(userPartyInfoDto);
        return partyDetailsService.updatedParty(userPartyInfoDto);
    }

    @DeleteMapping("/delete-party")
    public String deletePartyDetails(@RequestParam(name = "uniqueUserIdentifier") String uniqueUserIdentifier,
                                     @RequestParam(name = "partyIdentifierId") String partyIdentifierId) {
        return partyDetailsService.deletedParty(uniqueUserIdentifier, partyIdentifierId);
    }

    @GetMapping("/get-party-info")
    public UserPartyInfoDto getPartyDetails(@RequestParam(name = "uniqueUserIdentifier") String uniqueUserIdentifier,
                                            @RequestParam(name = "partyIdentifierId") String partyIdentifierId) {
        return partyDetailsService.getUserPartyInfo(uniqueUserIdentifier, partyIdentifierId);
    }

}
