package br.com.duxusdesafio.repository;

import br.com.duxusdesafio.model.ComposicaoTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para operações de persistência de {@link ComposicaoTime}.
 */
@Repository
public interface ComposicaoTimeRepository extends JpaRepository<ComposicaoTime, Long> {

    boolean existsByIntegranteId(Long integranteId);
}
