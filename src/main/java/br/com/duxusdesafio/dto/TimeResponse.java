package br.com.duxusdesafio.dto;

import br.com.duxusdesafio.model.Time;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO de resposta para {@link Time}.
 *
 * <p>Retorna apenas os dados relevantes do time, incluindo os nomes dos
 * integrantes em formato plano — sem expor a estrutura interna de
 * {@code ComposicaoTime}.</p>
 */
@Data
@AllArgsConstructor
public class TimeResponse {

    private Long id;
    private String nomeClube;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;

    /** Nomes dos integrantes que compõem o time. */
    private List<String> integrantes;

    /**
     * Converte uma entidade {@link Time} para este DTO.
     * Deve ser chamado dentro de um contexto transacional para que as
     * coleções lazily-loaded estejam disponíveis.
     *
     * @param time entidade a converter
     * @return DTO preenchido
     */
    public static TimeResponse from(Time time) {
        List<String> nomes = time.getComposicaoTimes().stream()
                .map(ct -> ct.getIntegrante().getNome())
                .collect(Collectors.toList());
        return new TimeResponse(time.getId(), time.getNomeClube(), time.getData(), nomes);
    }
}
