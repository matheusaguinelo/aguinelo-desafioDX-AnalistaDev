package br.com.duxusdesafio.exception;

/**
 * Lançada quando o intervalo de datas fornecido é inválido.
 * Resulta em resposta HTTP 400 Bad Request via {@link GlobalExceptionHandler}.
 *
 * <p>Condição: {@code dataInicial} posterior a {@code dataFinal}.</p>
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
