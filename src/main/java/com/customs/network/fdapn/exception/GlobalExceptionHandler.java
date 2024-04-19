package com.customs.network.fdapn.exception;

import com.amazonaws.services.alexaforbusiness.model.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<String> handleRecordNotFoundException(RecordNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(InvalidReferenceIdException.class)
    public ResponseEntity<String> handleInvalidReferenceIdException(InvalidReferenceIdException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchRecordException(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.OK).body(ex.getMessage());
    }
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleTokenExpirationException(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access to the resource.");
    }
    @ExceptionHandler(FdapnCustomExceptions.class)
    public ResponseEntity<ErrorDetails> handleFdapnCustomExceptions(FdapnCustomExceptions ex) {
        ErrorDetails details= new ErrorDetails();
        ErrorResCodes resCodes=ex.getResCodes();
        details.setErrorCode(resCodes.getErrorCode());
        details.setErrorCause(ex.getMessage());
        details.setErrorDesc(resCodes.getErrorMsg());
        return ResponseEntity.status(HttpStatus.OK).body(details);
    }


}
