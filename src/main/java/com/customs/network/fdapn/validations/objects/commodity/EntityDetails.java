package com.customs.network.fdapn.validations.objects.commodity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Valid
public class EntityDetails {
    @NotNull(message = "Entity data can not be null")
    private EntityData entityData;
    @NotNull(message = "entityAddress data can not be null")
    private EntityAddress entityAddress;
    @NotNull(message = "pointOfContacts data can not be null")
    private List<PointOfContact> pointOfContacts;
}


