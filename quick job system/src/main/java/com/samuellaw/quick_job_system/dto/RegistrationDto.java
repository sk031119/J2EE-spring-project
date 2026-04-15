// Requirements: Registration form DTO with validation for fullName, email, password, role
package com.samuellaw.quick_job_system.dto;

import com.samuellaw.quick_job_system.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    @NotNull(message = "Role is required")
    private Role role;

    // Employer-specific fields (optional, only when role=EMPLOYER)
    private String companyName;
    private String companyDescription;
    private String contactName;
}
