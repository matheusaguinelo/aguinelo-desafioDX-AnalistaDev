package br.com.duxusdesafio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de resposta para o endpoint {@code /api/time-da-data}.
 * Formato conforme o exemplo do README.
 */
@Data
@AllArgsConstructor
public class TimeDaDataResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    private String clube;

    /** Nomes dos integrantes que compõem o time. */
    private List<String> integrantes;
}
