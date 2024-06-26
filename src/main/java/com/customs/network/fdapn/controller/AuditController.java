package com.customs.network.fdapn.controller;

import com.customs.network.fdapn.dto.*;
import com.customs.network.fdapn.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/audit")
@CrossOrigin("http://localhost:5173")
public class AuditController {

    private final AuditService auditService;

    @Autowired
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/user-transaction")
    public ResponseEntity<FinalCount<TotalTransactionCountDto<?>>> getUserTransactionsForWeek(@RequestParam String userId,
                                                                         @RequestParam(required = false) String period) {

        FinalCount<TotalTransactionCountDto<?>> transactions = auditService.getUserTransactionsForPeriod(userId, period);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
    @GetMapping("/get-all-transaction")
    public FinalCount<TotalTransactionCountDto<?>> getAllTransactionsCount(@RequestParam(required = false) String userId,
                                              @RequestParam(required = false) String period){
        return auditService.getAllTransactionsCounts(userId,period);
    }
    @GetMapping("/getPortTransactionDetails")
    public List<PortInfoDto> getByUser(@RequestParam String userId,@RequestParam(required = false) String portName,
                                       @RequestParam(required = false) String portCode){
        return auditService.getPortTransactionInfoByUser(userId,portName,portCode);
    }
}
