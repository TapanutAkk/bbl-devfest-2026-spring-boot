package com.bbl.devfest.service;

import com.bbl.devfest.dto.UserRequest;
import com.bbl.devfest.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    @Test
    void isSeededWithSampleUsers() {
        assertThat(service.findAll())
                .hasSize(3)
                .extracting(User::username)
                .containsExactly("Bret", "Antonette", "Samantha");
    }

    @Test
    void createAssignsNextIdAndStoresUser() {
        User created = service.create(new UserRequest(
                "Patricia Lebsack", "Karianne", "Julianne.OConner@kory.org",
                "493-170-9623 x156", "kale.biz"));

        assertThat(created.id()).isEqualTo(4L);
        assertThat(service.findById(4L)).isEqualTo(created);
        assertThat(service.findAll()).hasSize(4);
    }

    @Test
    void findByIdUnknownThrows404() {
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(NOT_FOUND);
    }

    @Test
    void updateReplacesExistingUser() {
        User updated = service.update(1L, new UserRequest(
                "Leanne G.", "Bret", "leanne@example.com", null, null));

        assertThat(updated.id()).isEqualTo(1L);
        assertThat(service.findById(1L).email()).isEqualTo("leanne@example.com");
    }

    @Test
    void updateUnknownThrows404() {
        assertThatThrownBy(() -> service.update(99L, new UserRequest(
                "Nobody", "nobody", "nobody@example.com", null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(NOT_FOUND);
    }

    @Test
    void deleteRemovesUser() {
        service.delete(1L);

        assertThat(service.findAll()).hasSize(2);
        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deleteUnknownThrows404() {
        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode").isEqualTo(NOT_FOUND);
    }
}
