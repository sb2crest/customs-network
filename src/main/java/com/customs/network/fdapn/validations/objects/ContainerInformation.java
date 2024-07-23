package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContainerInformation {

    @Regex(value = RegexType.ALPHANUMERIC, message = " containerNumberOne must contain only alphanumeric characters.")
    @Size(max = 20 ,message = "containerNumberOne can hold maximum of {max} characters")
    private String containerNumberOne;

    @Regex(value = RegexType.ALPHANUMERIC, message = " containerNumberTwo must contain only alphanumeric characters.")
    @Size(max = 20 ,message = "containerNumberTwo can hold maximum of {max} characters")
    private String containerNumberTwo;

    @Regex(value = RegexType.ALPHANUMERIC, message = " containerNumberThree must contain only alphanumeric characters.")
    @Size(max = 20 ,message = "containerNumberThree can hold maximum of {max} characters")
    private String containerNumberThree;
}
