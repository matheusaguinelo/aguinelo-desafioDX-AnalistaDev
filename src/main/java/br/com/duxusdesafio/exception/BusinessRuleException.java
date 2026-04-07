package br.com.duxusdesafio.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio da aplicação.
 * Resulta em resposta HTTP 422 Unprocessable Entity via {@link GlobalExceptionHandler}.
 *
 * <p>Exemplos de uso:</p>
 * <ul>
 *   <li>Integrante duplicado no mesmo time</li>
 *   <li>Mesmo clube com dois times na mesma data</li>
 *   <li>Remoção de integrante vinculado a times ativos</li>
 * </ul>
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
