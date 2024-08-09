package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.validations.commodityvalidationimpl.DeclarationValidator;
import com.customs.network.fdapn.validations.objects.commodity.CourierTrackingAndDimensions;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;
import com.customs.network.fdapn.dto.UserPartyInfoDto;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.PartyDetailsService;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.customs.network.fdapn.validations.commodityvalidationimpl.CommonValidations;
import com.customs.network.fdapn.validations.commodityvalidationimpl.SegmentValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.commodity.EntityDetails;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import com.customs.network.fdapn.validations.objects.TransactionProductData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.customs.network.fdapn.utils.UtilMethods.*;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.checkInitialViolations;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

/**
 * Required Entire Refactoring
 * Processes and validates transaction-level data for Prior Notice (PN) submissions.
 * This class handles the detailed validation of product information, party details,
 * anticipated arrival information, product packaging, product conditions, and container information.
 * <p>
 * It converts JsonNode representations of transaction data into structured objects,
 * performs various validations based on program codes and business rules,
 * and aggregates validation errors.
 * <p>
 * Current implementation processes data in real-time for each request.
 * <p>
 * TODO: Consider implementing caching mechanisms in the future to improve performance,
 * especially for frequently accessed data or repetitive validations. Potential areas for caching include:
 * - Product information retrieved from UserProductInfoServices
 * - Party details retrieved from PartyDetailsService
 * - Validation results for common or repetitive data patterns
 *  - Consider caching the product json instead of again converting
 * <p>
 * Caching could significantly reduce database queries and processing time,
 * particularly for high-volume scenarios or when dealing with large datasets.
 * However, cache invalidation strategies must be carefully considered to ensure data consistency.
 */
@Component
@Slf4j
public class TransactionLevelValidations {

    private final ObjectMapper objectMapper;
    private final UserProductInfoServices productInfoServices;
    private final PartyDetailsService partyDetailsService;
    private final DeclarationValidator declarationValidator;
    private final Map<String, SegmentValidator> segmentValidators;
    private final Map<String, ConditionalValidator> conditionalValidators;

    public TransactionLevelValidations(ObjectMapper objectMapper,
                                       UserProductInfoServices productInfoServices,
                                       PartyDetailsService partyDetailsService,
                                       DeclarationValidator declarationValidator, List<SegmentValidator> segmentValidators,
                                       List<ConditionalValidator> conditionalValidatorList) {
        this.objectMapper = objectMapper;
        this.productInfoServices = productInfoServices;
        this.partyDetailsService = partyDetailsService;
        this.declarationValidator = declarationValidator;
        this.segmentValidators = segmentValidators.stream()
                .collect(Collectors.toMap(validation -> validation.getClass().getSimpleName().replace("CommodityValidator", "").toUpperCase(), Function.identity()));
        this.conditionalValidators = conditionalValidatorList.stream()
                .collect(Collectors.toMap(validation -> validation.getClass().getSimpleName().replace("CommodityConstants", "").toUpperCase(), Function.identity()));

    }

    public List<ValidationError> validateDataOnPNLevel(Declaration data, ExcelTransactionInfo info, List<String> productCodes) {
        List<ValidationError> errors = new ArrayList<>();
        try {
            List<TransactionProductData> transactionProductData = objectMapper.readValue(info.getTransactionProductDataString(), new TypeReference<>() {
            });
            info.setTransactionProductData(transactionProductData);
            if (info.getValidationErrors().isEmpty()) {
                List<ValidationError> validationErrors = declarationValidator.validate(data);
                if (validationErrors.isEmpty())
                    errors.addAll(mapInfoToProduct(data, transactionProductData, productCodes));
                errors.addAll(validationErrors);
            } else errors.addAll(info.getValidationErrors());
        } catch (JsonMappingException e) {
            String truncatedInput = truncateString(info.getTransactionProductDataString(), 50);
            errors.add(createValidationError(null, "Invalid transactionProductData on transaction serial number :" + info.getSlNo(), truncatedInput));
            return errors;
        } catch (JsonProcessingException e) {
            String truncatedInput = truncateString(info.getTransactionProductDataString(), 50);
            errors.add(createValidationError(null, "Error processing transactionProductData on transaction serial number: " + info.getSlNo(), truncatedInput));
        }

        return errors;
    }

    private List<ValidationError> mapInfoToProduct(Declaration declaration, List<TransactionProductData> transactionProductData, List<String> productCodes) {
        List<ValidationError> errors = new ArrayList<>();
        String uniqueUserIdentifier = declaration.getUniqueUserIdentifier();
        for (TransactionProductData transactionProduct : transactionProductData) {
            String productCode = transactionProduct.getProductIdentifier();
            UserProductInfoDto productByProductCode;
            productCodes.add(productCode);
            try {
                productByProductCode = productInfoServices.getProductByProductCode(uniqueUserIdentifier, productCode);
            } catch (FdapnCustomExceptions e) {
                log.error(e.getMessage());
                errors.add(createValidationError("productCodeNumber", e.getMessage(), null));
                break;
            }
            if (!productByProductCode.isValid()) {
                log.warn("the product by product code {} for user {} is not valid", productCode, uniqueUserIdentifier);
                continue;
            }

            JsonNode productDetailsJson = productByProductCode.getProductInfo();
            ProductDetails productDetails;
            try {
                productDetails = objectMapper.treeToValue(productDetailsJson, ProductDetails.class);
                List<ValidationError> validationErrors = validateSegments(declaration, transactionProduct, productDetails);
                errors.addAll(validationErrors);
            } catch (JsonProcessingException e) {
                String truncatedInput = truncateString(productDetailsJson.toString(), 50);
                errors.add(createValidationError(productCode, "Error processing product details on product code : " + productCode, truncatedInput));
            }
        }
        return errors;
    }

    private List<ValidationError> validateSegments(Declaration declaration, TransactionProductData transactionProductData, ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String programCode = productDetails.getGovernmentAgencyProgramCode();
        String productCode = productDetails.getProductCodeNumber();

        //Below can replace by the method getConditionalValidator method in the SegmentValidation
        ConditionalValidator conditionalValidator = supplyConditionalValidator(programCode);
        if (conditionalValidator == null) {
            log.error("ConditionalValidator not found for program code {} , occurred when executing validation for user {} and product {} ", programCode, declaration.getUniqueUserIdentifier(), productCode);
            errors.add(createValidationError(productCode, "An unexpected error occurred during validation. Please contact support.", "System Error"));
            return errors;
        }
        //consider creating a context inside the executeValidation
        CommonValidations.ValidationContext context = new CommonValidations.ValidationContext(productCode, errors, conditionalValidator, programCode.toUpperCase(), productDetails,declaration);
        executeValidations(context, transactionProductData, errors);
        return errors;
    }

    private void executeValidations(CommonValidations.ValidationContext context,
                                    TransactionProductData transactionProductData, List<ValidationError> errors) {

        List<EntityDetails> partyDetails = new ArrayList<>();
        String uniqueUserIdentifier = context.declaration().getUniqueUserIdentifier();
        for (String partyIdentifier : transactionProductData.getPartyIdentifiers()) {
            UserPartyInfoDto userPartyInfo = null;
            try {
                userPartyInfo = partyDetailsService.getUserPartyInfo(uniqueUserIdentifier, partyIdentifier);
                EntityDetails party = objectMapper.treeToValue(userPartyInfo.getPartyInfo(), EntityDetails.class);
                partyDetails.add(party);

            } catch (FdapnCustomExceptions e) {
                log.error(e.getMessage());
                errors.add(createValidationError(partyIdentifier, e.getMessage(), null));
            } catch (JsonProcessingException e) {
                String truncatedInput = truncateString(userPartyInfo.getPartyInfo().toString(), 20);
                errors.add(createValidationError(partyIdentifier, "Error processing party details on party identifier : " + partyIdentifier, truncatedInput));
            }
        }
        transactionProductData.setPartyDetails(partyDetails);
        SegmentValidator segmentValidator = supplySegmentValidator(context.programCode());
        if (segmentValidator == null) {
            log.error("SegmentValidator not found for program code {} , occurred when executing validation for user {} ", context.programCode(), context.declaration().getUniqueUserIdentifier());
            errors.add(createValidationError(null, "An unexpected error occurred during validation. Please contact support.", "System Error"));
            return;
        }
        if (!partyDetails.isEmpty()) {
            context.productDetails().setPartyDetails(partyDetails);
        }
        doValidate(context, transactionProductData, errors, segmentValidator);
    }

    private void doValidate(CommonValidations.ValidationContext context,
                            TransactionProductData transactionProductData,
                            List<ValidationError> errors,
                            SegmentValidator validator)  {
        validatePartyDetails(context, errors, validator);
        validateAnticipatedArrivalInformations(context, transactionProductData, errors, validator);
        validateProductPackaging(context, transactionProductData, errors, validator);
        validateProductCondition(context, transactionProductData, errors, validator);
        validateContainerInformation(context, transactionProductData, errors);
        validateLicensePlateIssuerAndNumber(context, transactionProductData, errors, validator);
        validateAffirmationOfCompliance(context, transactionProductData, errors, validator);
        validateCourierAndDimension(context, transactionProductData, errors, validator);
    }

    private void validateCourierAndDimension(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        CourierTrackingAndDimensions courierTrackingAndDimensions = transactionProductData.getCourierTrackingAndDimensions();
        context.productDetails().setCourierTrackingAndDimensions(courierTrackingAndDimensions);
        context.errors().addAll(checkInitialViolations(courierTrackingAndDimensions));
        validator.validateCourierTrackingAndDimensions(context);
    }

    private void validatePartyDetails(CommonValidations.ValidationContext context, List<ValidationError> errors, SegmentValidator validator) {
        List<EntityDetails> partyDetails = context.productDetails().getPartyDetails();
        if (isNullOrEmptyCollection(partyDetails)) {
            errors.add(createValidationError("partyDetails", "No party details found in the transaction product data for the product " + context.productCode(), partyDetails.toString()));
        } else {
            errors.addAll(checkInitialViolations(partyDetails));
            validator.validatePartyDetails(context);
        }
    }

    private void validateAnticipatedArrivalInformations(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        if (isNullOrEmptyCollection(transactionProductData.getAnticipatedArrivalInformations(), context.productDetails().getAnticipatedArrivalInformations())) {
            errors.add(createValidationError("anticipatedArrivalInformations", "This field is mandatory, But not provided in either basic product level or transactional Product level " + context.productCode(), transactionProductData.getAnticipatedArrivalInformations()));
        } else if (!isNullOrEmptyCollection(transactionProductData.getAnticipatedArrivalInformations())) {
            context.productDetails().setAnticipatedArrivalInformations(transactionProductData.getAnticipatedArrivalInformations());
            errors.addAll(checkInitialViolations(transactionProductData.getAnticipatedArrivalInformations()));
            validator.validateAnticipatedArrivalLocation(context);
        }
    }

    private void validateProductPackaging(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        if (isNullOrEmptyCollection(transactionProductData.getProductPackaging(), context.productDetails().getProductPackaging())) {
            errors.add(createValidationError("productPackaging", "This field is mandatory, But not provided in either basic product level or transactional Product level " + context.productCode(), transactionProductData.getProductPackaging()));
        } else if (!isNullOrEmptyCollection(transactionProductData.getProductPackaging())) {
            context.productDetails().setProductPackaging(transactionProductData.getProductPackaging());
            validator.validateProductPackaging(context);
        }
    }

    private void validateProductCondition(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        if (isNullOrEmptyCollection(transactionProductData.getProductCondition(), context.productDetails().getProductCondition())) {
            errors.add(createValidationError("productCondition", "This field is mandatory, But not provided in either basic product level or transactional Product level " + context.productCode(), transactionProductData.getProductCondition()));
        } else if (!isNullOrEmptyCollection(transactionProductData.getProductCondition())) {
            context.productDetails().setProductCondition(transactionProductData.getProductCondition());
            errors.addAll(checkInitialViolations(transactionProductData.getProductCondition()));
            validator.validateProductCondition(context);
        }
    }
    private void validateAffirmationOfCompliance(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        if (isNullOrEmptyCollection(transactionProductData.getAffirmationOfCompliance(), context.productDetails().getAffirmationOfCompliance())) {
            errors.add(createValidationError("affirmationOfCompliance", "This field is mandatory, But not provided in either basic product level or transactional Product level " + context.productCode(), transactionProductData.getAffirmationOfCompliance()));
        } else if (!isNullOrEmptyCollection(transactionProductData.getAffirmationOfCompliance())) {
            context.productDetails().setAffirmationOfCompliance(transactionProductData.getAffirmationOfCompliance());
            errors.addAll(checkInitialViolations(transactionProductData.getAffirmationOfCompliance()));
            validator.validateAffirmationOfCompliance(context);
        }
    }

    private void validateContainerInformation(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors) {
        if (isNullOrEmptyCollection(transactionProductData.getContainerInformation(), context.productDetails().getContainerInformation())) {
            errors.add(createValidationError("containerInformation", "This field is mandatory, But not provided in either basic product level or transactional Product level " + context.productCode(), transactionProductData.getContainerInformation()));
        } else if (!isNullOrEmptyCollection(transactionProductData.getContainerInformation())) {
            context.productDetails().setContainerInformation(transactionProductData.getContainerInformation());
            errors.addAll(checkInitialViolations(transactionProductData.getContainerInformation()));
        }
    }

    private void validateLicensePlateIssuerAndNumber(CommonValidations.ValidationContext context, TransactionProductData transactionProductData, List<ValidationError> errors, SegmentValidator validator) {
        if (!isNullObjects(transactionProductData.getLicensePlateIssuer() ,transactionProductData.getLicensePlateNumber())) {
            context.productDetails().setLicensePlateIssuer(transactionProductData.getLicensePlateIssuer());
            context.productDetails().setLicensePlateNumber(transactionProductData.getLicensePlateNumber());
            errors.addAll(checkInitialViolations(transactionProductData.getLicensePlateIssuer()));
            errors.addAll(checkInitialViolations(transactionProductData.getLicensePlateNumber()));
            validator.validateLicensePlateIssuer(context);
            validator.validateLicensePlateNumber(context);
        }
    }


    private SegmentValidator supplySegmentValidator(String programCode) {
        if (StringUtils.isBlank(programCode)) {
            return null;
        }
        if (segmentValidators.containsKey(programCode.toUpperCase())) {
            return segmentValidators.get(programCode.toUpperCase());
        }
        return null;
    }

    private ConditionalValidator supplyConditionalValidator(String programCode) {
        if (StringUtils.isBlank(programCode)) {
            return null;
        }
        if (conditionalValidators.containsKey(programCode.toUpperCase())) {
            return conditionalValidators.get(programCode.toUpperCase());
        }
        return null;
    }

}
