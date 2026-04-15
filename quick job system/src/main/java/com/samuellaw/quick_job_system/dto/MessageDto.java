// Requirements: Message form DTO with non-blank content validation
package com.samuellaw.quick_job_system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    @NotBlank(message = "Message content cannot be empty")
    private String content;
}
