package it.uniroma3.java.siw.controller.dto;

import it.uniroma3.java.siw.model.Recensione;

import java.time.LocalDateTime;

public record RecensioneDTO(
        Long id,
        Integer voto,
        String testo,
        LocalDateTime dataCreazione,
        String autoreUsername
) {
    public static RecensioneDTO from(Recensione r) {
        return new RecensioneDTO(
                r.getId(),
                r.getVoto(),
                r.getTesto(),
                r.getDataCreazione(),
                r.getUtente().getUsername()
        );
    }
}