package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.dto.JobSearchParams;
import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.enums.JobStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class JobPostSpecifications {

    private JobPostSpecifications() {
    }

    public static Specification<JobPost> openJobsMatching(JobSearchParams params) {
        return (root, query, cb) -> {
            List<Predicate> parts = new ArrayList<>();
            parts.add(cb.equal(root.get("status"), JobStatus.OPEN));

            if (StringUtils.hasText(params.getQ())) {
                String pattern = "%" + params.getQ().trim().toLowerCase() + "%";
                parts.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (StringUtils.hasText(params.getLocation())) {
                String pattern = "%" + params.getLocation().trim().toLowerCase() + "%";
                parts.add(cb.like(cb.lower(root.get("location")), pattern));
            }
            if (StringUtils.hasText(params.getJobType())) {
                parts.add(cb.equal(cb.lower(root.get("jobType")), params.getJobType().trim().toLowerCase()));
            }
            BigDecimal min = params.getMinPay();
            if (min != null) {
                parts.add(cb.ge(root.get("payRate"), min));
            }
            BigDecimal max = params.getMaxPay();
            if (max != null) {
                parts.add(cb.le(root.get("payRate"), max));
            }

            return cb.and(parts.toArray(new Predicate[0]));
        };
    }
}
