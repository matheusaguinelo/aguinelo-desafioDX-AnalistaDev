package br.com.duxusdesafio.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
     * Carregado de forma EAGER para que os métodos do {@code ApiService}
     * recebam os dados completos sem necessidade de queries adicionais.
     */
    @OneToMany(mappedBy = "time", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ComposicaoTime> composicaoTimes = new ArrayList<>();
}
