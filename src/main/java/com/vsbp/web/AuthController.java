package com.vsbp.web;

import com.vsbp.domain.AppUser;
import com.vsbp.service.BusinessException;
import com.vsbp.service.UserService;
import com.vsbp.web.form.LoginForm;
import com.vsbp.web.form.RegistrationForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult result,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (BusinessException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("success", "Registration successful. Please log in.");
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("form", new LoginForm());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("form") LoginForm form,
                        BindingResult result,
                        HttpServletRequest request,
                        Model model) {
        if (result.hasErrors()) {
            return "login";
        }
        Optional<AppUser> user = userService.authenticate(form.getEmail(), form.getPassword());
        if (user.isEmpty()) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
        HttpSession session = request.getSession(true);
        request.changeSessionId(); // prevents session fixation
        session.setAttribute(SessionUser.KEY, SessionUser.from(user.get()));
        return "redirect:/slots";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirect) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect.addFlashAttribute("success", "You have been logged out.");
        return "redirect:/login";
    }
}
