package br.com.duxusdesafio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Representa um integrante disponível para compor times.
 * Cada integrante possui um nome e uma função (ex: Meia, Atacante, Sniper).
 */
@Entity
@Table(name = "integrante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Integrante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório.")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "A função é obrigatória.")
    @Column(nullable = false)
    private String funcao;

    // equals/hashCode baseados no id para uso correto em coleções e streams
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Integrante)) return false;
        Integrante that = (Integrante) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
