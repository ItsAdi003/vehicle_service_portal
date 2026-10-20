package com.vsbp.service;

/** A rule of the domain was violated (duplicate e-mail, full slot, illegal status change ...). */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
