package com.ttn.redish.user;

import com.ttn.redish.common.HeavyPayload;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String name,
        @NotNull @Min(0) Integer age,
        @NotBlank String occupation,
        @NotNull HeavyPayload payload
) {
}
