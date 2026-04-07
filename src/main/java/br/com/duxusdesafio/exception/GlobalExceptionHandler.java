package br.com.duxusdesafio.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centraliza o tratamento de exceções da camada REST.
 *
 * <p>Garante que todos os erros retornem um {@link ErrorResponse} padronizado,
 * eliminando respostas inconsistentes entre os controllers.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Recurso não encontrado → 404 Not Found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        log.warn("Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Regra de negócio violada → 422 Unprocessable Entity.
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        log.warn("Regra de negócio violada: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse("BUSINESS_RULE_VIOLATION", ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Período de datas inválido → 400 Bad Request.
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleDateRange(InvalidDateRangeException ex) {
        log.warn("Período inválido: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("INVALID_DATE_RANGE", ex.getMessage(), LocalDateTime.now()));
    }

    /**
     * Falha na validação do Bean Validation (@Valid) → 400 Bad Request.
     * Consolida todos os erros de campo em uma única mensagem.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String campos = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validação falhou: {}", campos);
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", campos, LocalDateTime.now()));
    }

    /**
     * Qualquer outro erro não tratado → 500 Internal Server Error.
     * Loga o stack trace completo para facilitar o diagnóstico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro interno inesperado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "Erro interno do servidor.", LocalDateTime.now()));
    }
}
