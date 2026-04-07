package br.com.duxusdesafio.repository;

import br.com.duxusdesafio.model.Time;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para operações de persistência de {@link Time}.
 */
public interface TimeRepository extends JpaRepository<Time, Long> {

    /**
     * Busca todos os times com composição completa (JOIN FETCH via EntityGraph).
     * Usado pelos métodos de processamento do {@code ApiService} que precisam
     * iterar sobre todos os integrantes.
     */
    @EntityGraph(attributePaths = {"composicaoTimes", "composicaoTimes.integrante"})
    @Query("SELECT t FROM Time t")
    List<Time> findAllWithComposicao();

    /**
     * Busca paginada de times. As coleções são carregadas lazily dentro do
     * contexto transacional do service, com batching via {@code @BatchSize}.
     */
    Page<Time> findAll(Pageable pageable);

    /**
     * Busca um time por id com composição completa carregada via EntityGraph.
     * Sobrescreve o {@code findById} padrão para evitar LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"composicaoTimes", "composicaoTimes.integrante"})
    Optional<Time> findById(Long id);

    /**
     * Verifica se já existe um time do mesmo clube cadastrado para a data informada.
     * Regra de negócio: mesmo clube não pode ter dois times na mesma semana.
     */
    boolean existsByNomeClubeAndData(String nomeClube, LocalDate data);
}
