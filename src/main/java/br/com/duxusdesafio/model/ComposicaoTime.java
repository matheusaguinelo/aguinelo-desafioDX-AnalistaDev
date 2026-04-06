package br.com.duxusdesafio.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;

/**
 * Tabela de associação entre {@link Time} e {@link Integrante}.
 * Representa a participação de um integrante em um determinado time.
 */
@Entity
@Table(name = "composicao_time")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComposicaoTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_time", nullable = false)
    @JsonBackReference
    private Time time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_integrante", nullable = false)
    private Integrante integrante;
}
