// Requirements: Exception for resource not found (user, job, application)
package com.samuellaw.quick_job_system.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
