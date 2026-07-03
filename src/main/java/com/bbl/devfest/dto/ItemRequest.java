package com.bbl.devfest.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemRequest(@NotBlank String name) {
}
