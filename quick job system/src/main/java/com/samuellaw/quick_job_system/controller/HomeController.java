// Requirements: Home, login, register, and dashboard routing controller
// - Landing page at /
// - Login form at /login
// - Registration form at /register with POST
// - Dashboard redirect at /dashboard based on user role
package com.samuellaw.quick_job_system.controller;

import com.samuellaw.quick_job_system.dto.RegistrationDto;
import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.Role;
import com.samuellaw.quick_job_system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserService userService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        model.addAttribute("roles", new Role[]{Role.EMPLOYER, Role.WORKER});
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                               BindingResult result, Model model) {
        log.debug("Registration attempt for email: {}", dto.getEmail());

        // Check password match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registrationDto", "Passwords do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.EMPLOYER, Role.WORKER});
            return "register";
        }

        try {
            userService.registerUser(dto);
            log.info("User registered successfully: {}", dto.getEmail());
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed: {}", e.getMessage());
            result.rejectValue("email", "error.registrationDto", e.getMessage());
            model.addAttribute("roles", new Role[]{Role.EMPLOYER, Role.WORKER});
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        log.debug("Dashboard redirect for user: {}", authentication.getName());

        User user = userService.findByEmail(authentication.getName());

        return switch (user.getRole()) {
            case ADMIN -> "redirect:/admin/dashboard";
            case EMPLOYER -> "redirect:/employer/dashboard";
            case WORKER -> "redirect:/worker/jobs";
        };
    }
}
