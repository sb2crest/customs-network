package com.customs.network.fdapn.exception;

import lombok.Data;
import lombok.Getter;

@Getter
public class FdapnCustomExceptions extends RuntimeException{
    private final ErrorResCodes resCodes;
    public FdapnCustomExceptions(ErrorResCodes resCodes){
        super(resCodes.getErrorMsg());
        this.resCodes=resCodes;
    }
    public FdapnCustomExceptions(ErrorResCodes resCodes,String message){
        super(message);
        this.resCodes=resCodes;

    }
}
