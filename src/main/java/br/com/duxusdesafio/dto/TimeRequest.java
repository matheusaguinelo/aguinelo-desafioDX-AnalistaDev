package br.com.duxusdesafio.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para criação de um novo Time via API.
 * Contém os dados básicos do time e os IDs dos integrantes que o compõem.
 */
@Data
public class TimeRequest {

    @NotBlank(message = "O nome do clube é obrigatório.")
    private String nomeClube;

    @NotNull(message = "A data é obrigatória.")
    private LocalDate data;

    /** IDs dos integrantes que farão parte deste time. */
    @NotEmpty(message = "O time deve ter ao menos um integrante.")
    private List<Long> integranteIds;
}
