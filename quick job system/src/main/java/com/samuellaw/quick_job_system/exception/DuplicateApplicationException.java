// Requirements: Exception for duplicate job application attempts
package com.samuellaw.quick_job_system.exception;

public class DuplicateApplicationException extends RuntimeException {

    public DuplicateApplicationException(String message) {
        super(message);
    }
}
