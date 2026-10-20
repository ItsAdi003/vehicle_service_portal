package com.vsbp;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:authweb;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class AuthWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerPageIsShown() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Create your account")));
    }

    @Test
    void validRegistrationRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Asha Rao")
                        .param("email", uniqueEmail())
                        .param("password", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void shortPasswordIsRejectedWithFieldError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Asha Rao")
                        .param("email", uniqueEmail())
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Password must be 8 to 72 characters")));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/register")
                .param("fullName", "Asha Rao").param("email", email).param("password", "Secret123"));

        mockMvc.perform(post("/register")
                        .param("fullName", "Someone Else").param("email", email).param("password", "Secret123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already registered")));
    }

    @Test
    void loginWithCorrectCredentialsRedirectsHome() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/register")
                .param("fullName", "Asha Rao").param("email", email).param("password", "Secret123"));

        mockMvc.perform(post("/login").param("email", email).param("password", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginWithWrongPasswordShowsError() throws Exception {
        mockMvc.perform(post("/login").param("email", "admin@vsbp.local").param("password", "wrong-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid email or password")));
    }

    @Test
    void seededAdminCanLogIn() throws Exception {
        mockMvc.perform(post("/login").param("email", "admin@vsbp.local").param("password", "Admin@123"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void invalidPhoneNumberIsRejected() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Asha Rao")
                        .param("email", uniqueEmail())
                        .param("phone", "abc")
                        .param("password", "Secret123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enter a valid phone number")));
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
