package br.com.duxusdesafio.dto;

import br.com.duxusdesafio.model.Integrante;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO de resposta para {@link Integrante}.
 *
 * <p>Desacopla a entidade JPA da representação JSON exposta pela API,
 * evitando que campos internos sejam serializados acidentalmente.</p>
 */
@Data
@AllArgsConstructor
public class IntegranteResponse {

    private Long id;
    private String nome;
    private String funcao;

    /**
     * Converte uma entidade {@link Integrante} para este DTO.
     *
     * @param integrante entidade a converter
     * @return DTO preenchido
     */
    public static IntegranteResponse from(Integrante integrante) {
        return new IntegranteResponse(
                integrante.getId(),
                integrante.getNome(),
                integrante.getFuncao()
        );
    }
}
