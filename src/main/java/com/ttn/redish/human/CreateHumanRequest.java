package com.ttn.redish.human;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateHumanRequest(
        @NotBlank String name,
        @NotNull @Min(0) Integer age,
        @NotBlank String occupation
) {
}
