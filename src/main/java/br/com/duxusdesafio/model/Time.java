package br.com.duxusdesafio.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um time escalado para uma semana específica.
 * Um time pertence a um clube e possui uma data de referência.
 * A composição (integrantes) é armazenada via {@link ComposicaoTime}.
 */
@Entity
@Table(name = "time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do clube é obrigatório.")
    @Column(name = "nome_clube", nullable = false)
    private String nomeClube;

    @NotNull(message = "A data é obrigatória.")
    @Column(nullable = false)
    private LocalDate data;

    /**
     * Composição do time: lista de vínculos com integrantes.
     *
     * <p>Usa {@code FetchType.LAZY} para evitar N+1 queries ao listar times.
     * O {@code @BatchSize(size = 50)} instrui o Hibernate a carregar as coleções
     * em lotes quando necessário, reduzindo drasticamente o número de queries.</p>
     */
    @OneToMany(mappedBy = "time", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 50)
    @JsonManagedReference
    @Builder.Default
    private List<ComposicaoTime> composicaoTimes = new ArrayList<>();
}
