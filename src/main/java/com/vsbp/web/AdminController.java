package com.vsbp.web;

import com.vsbp.service.BookingService;
import com.vsbp.service.BusinessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Workshop-side actions. The AuthInterceptor already restricts /admin/** to the ADMIN role. */
@Controller
@RequestMapping("/admin/bookings")
public class AdminController {

    private final BookingService bookingService;

    public AdminController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("bookings", bookingService.all());
        return "admin-bookings";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            bookingService.confirm(id);
            redirect.addFlashAttribute("success", "Booking #" + id + " confirmed.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            bookingService.complete(id);
            redirect.addFlashAttribute("success", "Booking #" + id + " marked as completed.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/bookings";
    }
}
