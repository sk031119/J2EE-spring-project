// Requirements: Exception for invalid status transitions (e.g. approving a cancelled application)
package com.samuellaw.quick_job_system.exception;

public class InvalidStatusException extends RuntimeException {

    public InvalidStatusException(String message) {
        super(message);
    }
}
