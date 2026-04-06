package br.com.duxusdesafio.repository;

import br.com.duxusdesafio.model.Integrante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para operações de persistência de {@link Integrante}.
 */
@Repository
public interface IntegranteRepository extends JpaRepository<Integrante, Long> {
}
