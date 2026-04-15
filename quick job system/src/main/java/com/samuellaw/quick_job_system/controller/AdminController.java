// Requirements: Admin web controller
// - Dashboard with platform stats
// - View all users, toggle enable/disable
// - View all job posts, delete inappropriate ones
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.entity.JobPost;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.service.JobApplicationService;
import com.samuellaw.quick_job_system.service.JobPostService;
import com.samuellaw.quick_job_system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final JobPostService jobPostService;
    private final JobApplicationService jobApplicationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> users = userService.findAll();
        List<JobPost> jobs = jobPostService.findAll();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalJobs", jobs.size());
        model.addAttribute("totalApplications", jobApplicationService.findAll().size());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.toggleEnabled(id);
        redirectAttributes.addFlashAttribute("successMessage", "User status updated");
        return "redirect:/admin/users";
    }

    @GetMapping("/jobs")
    public String jobList(Model model) {
        model.addAttribute("jobs", jobPostService.findAll());
        return "admin/jobs";
    }

    @PostMapping("/jobs/{id}/delete")
    public String deleteJob(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        jobPostService.adminDelete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Job post removed");
        return "redirect:/admin/jobs";
    }
}
