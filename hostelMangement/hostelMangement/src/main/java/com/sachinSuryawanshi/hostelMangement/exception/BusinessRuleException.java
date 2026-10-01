package com.sachinSuryawanshi.hostelMangement.exception;

// thrown when a request is well-formed but breaks a hospital rule (double booking, duplicate email, ...)
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
