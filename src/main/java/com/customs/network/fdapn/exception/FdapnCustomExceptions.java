package com.customs.network.fdapn.exception;

import com.customs.network.fdapn.model.ValidationError;
import lombok.Getter;
import java.util.List;

@Getter
public class FdapnCustomExceptions extends RuntimeException{
    private final ErrorResCodes resCodes;
    private  List<ValidationError> errorList;
    public FdapnCustomExceptions(ErrorResCodes resCodes){
        this.resCodes=resCodes;
    }
    public FdapnCustomExceptions(ErrorResCodes resCodes,String message){
        super(message);
        this.resCodes=resCodes;
    }
    public FdapnCustomExceptions(ErrorResCodes resCodes,List<ValidationError> errorList){
        this.resCodes=resCodes;
        this.errorList=errorList;
    }
    public FdapnCustomExceptions(ErrorResCodes resCodes, List<ValidationError> errorList, String message){
        super(message);
        this.resCodes=resCodes;
        this.errorList=errorList;
    }
}
