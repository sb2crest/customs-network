package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.validations.constants.GeneralValidationConstants;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.checkInitialViolations;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Service
@Slf4j
public class ValidationEntryPoint {
    private final ObjectMapper objectMapper;
    private final Map<String, CommodityValidator> commodityValidators;
    private final TransactionLevelValidations transactionLevelValidations;

    public ValidationEntryPoint(ObjectMapper objectMapper,
                                List<CommodityValidator> commodityValidatorList,
                                TransactionLevelValidations transactionLevelValidations) {
        this.objectMapper = objectMapper;
        this.commodityValidators = commodityValidatorList.stream()
                .collect(Collectors.toMap(validation -> validation.getClass().getSimpleName().replace("CommodityValidator", "").toUpperCase(), Function.identity()));
        this.transactionLevelValidations = transactionLevelValidations;
    }

    public List<ValidationError> validateProduct(JsonNode productInfo) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        List<ValidationError> errors = new ArrayList<>();
        ProductDetails product = objectMapper.treeToValue(productInfo, ProductDetails.class);
        String programCode = product.getGovernmentAgencyProgramCode();
        if (StringUtils.isNotBlank(product.getGovernmentAgencyProgramCode()) && GeneralValidationConstants.isValidProgramCode(programCode.toUpperCase())) {
            if (commodityValidators.containsKey(programCode.toUpperCase())) {
                errors.addAll(checkInitialViolations(product));
                CommodityValidator validator = commodityValidators.get(programCode);
                List<ValidationError> validationErrors = validator.validate(product);
                errors.addAll(validationErrors);
            }
        } else {
            errors.add(createValidationError(product.getProductCodeNumber(), "governmentAgencyProgramCode", "Invalid governmentAgencyProgramCode", programCode));
        }
        long endTime = System.currentTimeMillis();
        log.info("Validation took {} milli seconds to complete", endTime - start);
        return errors;
    }

    public List<ExcelValidationResponse> validateExcelTransactions(List<ExcelTransactionInfo> transactions) {
        return transactions.stream()
                .filter(Objects::nonNull)
                .map(obj -> {
                    ExcelValidationResponse response = new ExcelValidationResponse();
                    PriorNoticeData priorNoticeData = obj.getPriorNoticeData();
                    List<String> productCodes = new ArrayList<>();
                    List<ValidationError> validationErrors = transactionLevelValidations.validateDataOnPNLevel(priorNoticeData, obj, productCodes);
                    List<ValidationError> validationErrorList = new ArrayList<>(validationErrors);
                    obj.setProductCode(productCodes);
                    response.setExcelTransactionInfo(obj);
                    response.setValidationErrorList(validationErrorList);
                    return response;
                }).toList();
    }
}
