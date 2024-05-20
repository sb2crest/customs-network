package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.service.UserProductInfoServices;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class UserProductController {
    private final UserProductInfoServices customerProductInfoService;

    public UserProductController(UserProductInfoServices customerProductInfoService) {
        this.customerProductInfoService = customerProductInfoService;
    }

    @PostMapping("/save")
    public String save(@RequestBody UserProductInfoDto customerProductInfo) {
        return customerProductInfoService.save(customerProductInfo);
    }
    @GetMapping("/get-product")
    public UserProductInfoDto get(@RequestParam("code") String productCode){
        return customerProductInfoService.getProductByProductCode(productCode);
    }
}
