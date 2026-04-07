package br.com.duxusdesafio.exception;

/**
 * Lançada quando um recurso solicitado não é encontrado no banco de dados.
 * Resulta em resposta HTTP 404 Not Found via {@link GlobalExceptionHandler}.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
