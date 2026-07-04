package com.bbl.devfest.controller;

import com.bbl.devfest.dto.UserRequest;
import com.bbl.devfest.model.User;
import com.bbl.devfest.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private static final User LEANNE = new User(1L, "Leanne Graham", "Bret",
            "Sincere@april.biz", "1-770-736-8031 x56442", "hildegard.org");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @Test
    void listReturnsUsers() throws Exception {
        given(service.findAll()).willReturn(List.of(LEANNE));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("Bret"));
    }

    @Test
    void getReturnsUser() throws Exception {
        given(service.findById(1L)).willReturn(LEANNE);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Leanne Graham"))
                .andExpect(jsonPath("$.email").value("Sincere@april.biz"));
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        given(service.findById(99L))
                .willThrow(new ResponseStatusException(NOT_FOUND, "User 99 not found"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201() throws Exception {
        given(service.create(any(UserRequest.class))).willReturn(LEANNE);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Leanne Graham","username":"Bret","email":"Sincere@april.biz"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Leanne Graham"));
    }

    @Test
    void createWithMissingRequiredFieldsReturns400WithMessages() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages", hasItem("name must not be blank")))
                .andExpect(jsonPath("$.messages", hasItem("username must not be blank")))
                .andExpect(jsonPath("$.messages", hasItem("email must not be blank")));
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        User updated = new User(1L, "Leanne G.", "Bret",
                "Sincere@april.biz", null, null);
        given(service.update(eq(1L), any(UserRequest.class))).willReturn(updated);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Leanne G.","username":"Bret","email":"Sincere@april.biz"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Leanne G."));
    }

    @Test
    void updateUnknownUserReturns404() throws Exception {
        given(service.update(eq(99L), any(UserRequest.class)))
                .willThrow(new ResponseStatusException(NOT_FOUND, "User 99 not found"));

        mockMvc.perform(put("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nobody","username":"nobody","email":"nobody@example.com"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void deleteUnknownUserReturns404() throws Exception {
        willThrow(new ResponseStatusException(NOT_FOUND, "User 99 not found"))
                .given(service).delete(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}
