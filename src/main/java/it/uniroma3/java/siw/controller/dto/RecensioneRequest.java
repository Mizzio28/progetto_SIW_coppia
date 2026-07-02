package it.uniroma3.java.siw.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecensioneRequest(
        @NotNull(message = "Il voto è obbligatorio")
        @Min(value = 1, message = "Il voto minimo è 1")
        @Max(value = 5, message = "Il voto massimo è 5")
        Integer voto,

        @Size(max = 500, message = "Il testo non può superare i 500 caratteri")
        String testo
) {}