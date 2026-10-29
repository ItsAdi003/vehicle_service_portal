package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vsbp.domain.Booking;
import com.vsbp.repository.AppUserRepository;
import com.vsbp.repository.BookingRepository;
import com.vsbp.repository.ServiceSlotRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end flow through the real controllers, services and an in-memory database (no browser). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bookingweb;DB_CLOSE_DELAY=-1",
        "app.seed.slot-capacity=50"})
@AutoConfigureMockMvc
class BookingWebTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository users;
    @Autowired
    private BookingRepository bookings;
    @Autowired
    private ServiceSlotRepository slots;

    private record Customer(String email, MockHttpSession session) { }

    private Customer registerAndLogin() throws Exception {
        String email = "customer-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/register")
                .param("fullName", "Test Customer").param("email", email).param("password", "Secret123"));
        return new Customer(email, login(email, "Secret123"));
    }

    private MockHttpSession login(String email, String password) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login").param("email", email).param("password", password))
                .andReturn().getRequest().getSession();
    }

    private Long bookFirstSlot(Customer customer) throws Exception {
        Long slotId = slots.findAll().get(0).getId();
        mockMvc.perform(post("/bookings").session(customer.session())
                        .param("slotId", String.valueOf(slotId))
                        .param("vehicleNumber", "mh12ab1234")
                        .param("vehicleModel", "Honda City")
                        .param("serviceType", "OIL_CHANGE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));
        Long userId = users.findByEmailIgnoreCase(customer.email()).orElseThrow().getId();
        return bookings.findByUserIdOrderByIdDesc(userId).get(0).getId();
    }

    @Test
    void guestIsRedirectedToLoginFromBookings() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void customerBooksSlotAndSeesItAsRequested() throws Exception {
        Customer customer = registerAndLogin();
        bookFirstSlot(customer);

        mockMvc.perform(get("/bookings").session(customer.session()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MH12AB1234")))
                .andExpect(content().string(containsString("REQUESTED")));
    }

    @Test
    void bookingWithoutVehicleNumberShowsValidationError() throws Exception {
        Customer customer = registerAndLogin();
        Long slotId = slots.findAll().get(0).getId();

        mockMvc.perform(post("/bookings").session(customer.session())
                        .param("slotId", String.valueOf(slotId))
                        .param("vehicleModel", "Honda City")
                        .param("serviceType", "OIL_CHANGE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vehicle number is required")));
    }

    @Test
    void customerCannotOpenAdminPages() throws Exception {
        Customer customer = registerAndLogin();

        mockMvc.perform(get("/admin/bookings").session(customer.session()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConfirmsAndCustomerSeesConfirmedStatusWithTimeline() throws Exception {
        Customer customer = registerAndLogin();
        Long bookingId = bookFirstSlot(customer);
        MockHttpSession admin = login("admin@vsbp.local", "Admin@123");

        mockMvc.perform(post("/admin/bookings/" + bookingId + "/confirm").session(admin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/bookings"));

        mockMvc.perform(get("/bookings/" + bookingId).session(customer.session()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CONFIRMED")))
                .andExpect(content().string(containsString("timeline-confirmed")));
    }

    @Test
    void customerCancelsOwnBooking() throws Exception {
        Customer customer = registerAndLogin();
        Long bookingId = bookFirstSlot(customer);

        mockMvc.perform(post("/bookings/" + bookingId + "/cancel").session(customer.session()))
                .andExpect(status().is3xxRedirection());

        Booking booking = bookings.findById(bookingId).orElseThrow();
        assertThat(booking.getStatus().name()).isEqualTo("CANCELLED");
    }

    @Test
    void anotherCustomerCannotSeeTheBooking() throws Exception {
        Customer owner = registerAndLogin();
        Long bookingId = bookFirstSlot(owner);
        Customer stranger = registerAndLogin();

        mockMvc.perform(get("/bookings/" + bookingId).session(stranger.session()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));
    }

    @Test
    void bookingFormWithoutSlotRedirectsToSlotsWithMessage() throws Exception {
        Customer customer = registerAndLogin();

        mockMvc.perform(get("/bookings/new").session(customer.session()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/slots"));
    }

    @Test
    void adminCannotBookASlot() throws Exception {
        MockHttpSession admin = login("admin@vsbp.local", "Admin@123");
        Long slotId = slots.findAll().get(0).getId();

        mockMvc.perform(get("/bookings/new").param("slotId", String.valueOf(slotId)).session(admin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/slots"));
        mockMvc.perform(get("/slots").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("book-link"))));
    }

    @Test
    void adminLoginLandsOnAdminList() throws Exception {
        mockMvc.perform(post("/login").param("email", "admin@vsbp.local").param("password", "Admin@123"))
                .andExpect(redirectedUrl("/admin/bookings"));
    }
}
