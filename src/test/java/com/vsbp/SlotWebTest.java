package com.vsbp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vsbp.repository.ServiceSlotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:slotweb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class SlotWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceSlotRepository slotRepository;

    @Test
    void startupCreatesFourSlotsPerDayForSevenDays() {
        assertThat(slotRepository.count()).isGreaterThanOrEqualTo(28);
    }

    @Test
    void guestIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/slots"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void loggedInUserSeesSlotTable() throws Exception {
        MockHttpSession session = loginAsAdmin();

        mockMvc.perform(get("/slots").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("slots-table")))
                .andExpect(content().string(containsString("seats-left")));
    }

    private MockHttpSession loginAsAdmin() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/login")
                        .param("email", "admin@vsbp.local")
                        .param("password", "Admin@123"))
                .andReturn().getRequest().getSession();
    }
}
