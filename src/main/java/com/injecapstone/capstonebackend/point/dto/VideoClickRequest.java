package com.injecapstone.capstonebackend.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VideoClickRequest(
        @NotBlank
        @Size(max = 64)
        @Schema(example = "20260823LGOB")
        String gameId
) {
}