package com.smartshelfx.exception;

/**
 * ResourceNotFoundException
 * 
 * Thrown when an entity (such as a Product or Category) cannot be found by its ID or identifier.
 * Results in an HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
