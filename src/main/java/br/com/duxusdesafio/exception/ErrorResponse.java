package br.com.duxusdesafio.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO padrão para respostas de erro da API.
 *
 * <p>Formato JSON:</p>
 * <pre>
 * {
 *   "codigo": "NOT_FOUND",
 *   "mensagem": "Integrante não encontrado com id: 99",
 *   "timestamp": "2024-01-07T10:30:00"
 * }
 * </pre>
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    /** Código semântico do erro (ex: NOT_FOUND, VALIDATION_ERROR). */
    private String codigo;

    /** Mensagem legível descrevendo o problema. */
    private String mensagem;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
