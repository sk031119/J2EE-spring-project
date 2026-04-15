// Requirements: JobPost form DTO with validation for title, payRate, startTime, etc.
package com.samuellaw.quick_job_system.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostDto {

    private Long id;

    @NotBlank(message = "Job title is required")
    private String title;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Pay rate is required")
    @Positive(message = "Pay rate must be greater than 0")
    private BigDecimal payRate;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    private String requiredSkills;

    @Positive(message = "Workers needed must be greater than 0")
    @Builder.Default
    private int workersNeeded = 1;
}
