package com.samuellaw.quick_job_system.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobSearchParams {

    private String q = "";
    private String location = "";
    private String jobType = "";
    private BigDecimal minPay;
    private BigDecimal maxPay;
}
