package com.smartshelfx.exception;

/**
 * BadRequestException
 * 
 * Thrown when business rules are violated (e.g., negative stock, invalid order state transition).
 * Results in an HTTP 400 Bad Request response.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
