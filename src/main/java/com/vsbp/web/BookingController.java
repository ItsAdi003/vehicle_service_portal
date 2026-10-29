package com.vsbp.web;

import com.vsbp.domain.Booking;
import com.vsbp.domain.ServiceType;
import com.vsbp.service.BookingService;
import com.vsbp.service.BusinessException;
import com.vsbp.service.SlotAvailability;
import com.vsbp.service.SlotService;
import com.vsbp.web.form.BookingForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final SlotService slotService;

    public BookingController(BookingService bookingService, SlotService slotService) {
        this.bookingService = bookingService;
        this.slotService = slotService;
    }

    @GetMapping("/new")
    public String newBooking(@RequestParam(value = "slotId", required = false) Long slotId,
                             @SessionAttribute(SessionUser.KEY) SessionUser me,
                             Model model,
                             RedirectAttributes redirect) {
        try {
            rejectAdmin(me);
            BookingForm form = new BookingForm();
            form.setSlotId(slotId);
            return showForm(form, model);
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/slots";
        }
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookingForm form,
                         BindingResult result,
                         @SessionAttribute(SessionUser.KEY) SessionUser me,
                         Model model,
                         RedirectAttributes redirect) {
        try {
            rejectAdmin(me);
            if (result.hasErrors()) {
                return showForm(form, model);
            }
            Booking booking = bookingService.request(me.getId(), form);
            redirect.addFlashAttribute("success",
                    "Booking #" + booking.getId() + " requested. The workshop will confirm it shortly.");
            return "redirect:/bookings";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/slots";
        }
    }

    @GetMapping
    public String myBookings(@SessionAttribute(SessionUser.KEY) SessionUser me, Model model) {
        model.addAttribute("bookings", bookingService.forUser(me.getId()));
        return "bookings";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable("id") Long id,
                          @SessionAttribute(SessionUser.KEY) SessionUser me,
                          Model model,
                          RedirectAttributes redirect) {
        try {
            model.addAttribute("booking", bookingService.getForViewer(id, me.getId(), me.isAdmin()));
            return "booking-details";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/bookings";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long id,
                         @SessionAttribute(SessionUser.KEY) SessionUser me,
                         RedirectAttributes redirect) {
        try {
            bookingService.cancel(id, me.getId());
            redirect.addFlashAttribute("success", "Booking #" + id + " cancelled. The seat is free again.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/bookings";
    }

    /** Bookings are made by customers; the workshop admin only confirms and completes them. */
    private static void rejectAdmin(SessionUser me) {
        if (me.isAdmin()) {
            throw new BusinessException("Admins manage bookings and cannot book slots themselves");
        }
    }

    /** Fills the model for the booking form; throws BusinessException if the slot is unknown, past or full. */
    private String showForm(BookingForm form, Model model) {
        if (form.getSlotId() == null) {
            throw new BusinessException("Please choose a slot first");
        }
        SlotAvailability availability = slotService.availability(form.getSlotId());
        if (slotService.hasStarted(availability.getSlot())) {
            throw new BusinessException("This slot is in the past");
        }
        if (availability.isFull()) {
            throw new BusinessException("This slot is fully booked");
        }
        model.addAttribute("form", form);
        model.addAttribute("slot", availability.getSlot());
        model.addAttribute("serviceTypes", ServiceType.values());
        return "booking-form";
    }
}
