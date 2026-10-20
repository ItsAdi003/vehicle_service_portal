package com.vsbp.web;

import com.vsbp.service.SlotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping("/slots")
    public String slots(Model model) {
        model.addAttribute("slots", slotService.upcomingSlots());
        return "slots";
    }
}
