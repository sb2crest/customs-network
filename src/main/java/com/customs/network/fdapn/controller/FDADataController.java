package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.PartialCodeRequest;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.service.FDADataService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.customs.network.fdapn.exception.ErrorResCodes.SERVER_ERROR;
import static com.customs.network.fdapn.exception.ErrorResCodes.SERVICE_UNAVAILABLE;

@RestController
@RequestMapping("/fda")
public class FDADataController {

    private final FDADataService fdaDataService;

    @Autowired
    public FDADataController(FDADataService fdaDataService) {
        this.fdaDataService = fdaDataService;
    }

    @GetMapping("/industry")
    public ResponseEntity<JsonNode> fetchIndustryData() {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getAllIndustry();
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/industry/{industryId}")
    public ResponseEntity<JsonNode> fetchByIndustryId(@PathVariable("industryId") Integer id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByIndustryId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/subclass")
    public ResponseEntity<JsonNode> fetchSubclassData() {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getSubClassData();
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/subclass/{subclassId}")
    public ResponseEntity<JsonNode> fetchBySubClassId(@PathVariable("subclassId") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getBySubclassId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/industrysubclass/{industryid}")
    public ResponseEntity<JsonNode> fetchByIndustrySubclass(@PathVariable("industryid") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByIndustrySubclassId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/class")
    public ResponseEntity<JsonNode> fetchClass() {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.fetchClass();
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<JsonNode> fetchByClassId(@PathVariable("classId") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByClassId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/industryclass/{industryid}")
    public ResponseEntity<JsonNode> fetchByIndustryClassId(@PathVariable("industryid") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByIndustryClassId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/pic")
    public ResponseEntity<JsonNode> fetchPic() {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getPic();
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/pic/{picId}")
    public ResponseEntity<JsonNode> fetchByPicId(@PathVariable("picId") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByPicId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/industryPic/{industryId}")
    public ResponseEntity<JsonNode> fetchByIndustryPicId(@PathVariable("industryId") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByIndustryPic(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/product")
    public ResponseEntity<JsonNode> fetchProduct() {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getProduct();
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<JsonNode> fetchByProductId(@PathVariable("productId") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByProductId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/product/name/{name}")
    public ResponseEntity<JsonNode> fetchByProductName(@PathVariable("name") String name) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByProductName(name);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @PostMapping("/product/name")
    public ResponseEntity<JsonNode> fetchByProductNameFormData(@RequestParam String name) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getProductNameByFormData(name);
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                throw new FdapnCustomExceptions(SERVICE_UNAVAILABLE,"Service Unavailable: FDA API is currently unavailable");
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch product name");
            }
        });
    }

    @GetMapping("/industryproduct/{industryid}")
    public ResponseEntity<JsonNode> fetchByIndustryProductByIndustryId(@PathVariable("industryid") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getByIndustryProductByIndustryId(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/productCode/{code}")
    public ResponseEntity<JsonNode> ValidateByProductCode(@PathVariable("code") String code) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.validateProductCode(code);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @GetMapping("/productcodeindustry/{industryid}")
    public ResponseEntity<JsonNode> getProductCodesByIndustry(@PathVariable("industryid") String id) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getProductCodeByIndustry(id);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR,"Failed to fetch..");
            }
        });
    }

    @PostMapping("/partialCode")
    public ResponseEntity<JsonNode> getPartialCodes(@RequestBody PartialCodeRequest request) {
        return fetchAndConstructResponse(() -> {
            try {
                return fdaDataService.getCodes(request);
            } catch (IOException e) {
                throw new FdapnCustomExceptions(SERVER_ERROR, "Failed to fetch partial codes");
            }
        });
    }

    private ResponseEntity<JsonNode> fetchAndConstructResponse(Supplier<JsonNode> dataSupplier) {
        return Optional.ofNullable(dataSupplier.get())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
