package com.smartshelfx.exception;

/**
 * DuplicateResourceException
 * 
 * Thrown when trying to create a resource with a unique field that already exists
 * (for example, duplicate SKU or category name).
 * Results in an HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
