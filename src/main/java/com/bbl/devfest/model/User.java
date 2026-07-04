package com.bbl.devfest.model;

/**
 * User payload matching the simplified jsonplaceholder.typicode.com/users shape.
 * Stored in memory (no JPA) — see UserService.
 */
public record User(
        Long id,
        String name,
        String username,
        String email,
        String phone,
        String website) {
}
