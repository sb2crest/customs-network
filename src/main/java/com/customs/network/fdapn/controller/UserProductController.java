package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.service.UserProductInfoServices;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class UserProductController {
    private final UserProductInfoServices customerProductInfoService;

    public UserProductController(UserProductInfoServices customerProductInfoService) {
        this.customerProductInfoService = customerProductInfoService;
    }

    @PostMapping("/save")
    public String save(@RequestBody UserProductInfoDto customerProductInfo) {
        return customerProductInfoService.saveProduct(customerProductInfo);
    }
    @GetMapping("/get")
    public UserProductInfoDto get(@RequestParam("code") String productCode,
                                  @RequestParam("uniqueUserIdentifier") String uniqueUserIdentifier
    ){
        return customerProductInfoService.getProductByProductCode(uniqueUserIdentifier,productCode);
    }

    @GetMapping("/list-all-codes")
    public List<String> listAllCodes(@RequestParam("uniqueUserIdentifier") String uniqueUserIdentifier){
        return customerProductInfoService.getProductCodeList(uniqueUserIdentifier);
    }

    @PutMapping("/update")
    public String update(@RequestBody UserProductInfoDto customerProductInfo){
        return customerProductInfoService.updateProductInfo(customerProductInfo);
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam("code") String productCode,
                         @RequestParam("uniqueUserIdentifier") String uniqueUserIdentifier
    ){
        return customerProductInfoService.deleteProduct(uniqueUserIdentifier,productCode);
    }
}
