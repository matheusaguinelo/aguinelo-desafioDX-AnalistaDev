package br.com.duxusdesafio.service;

import br.com.duxusdesafio.dto.IntegranteResponse;
import br.com.duxusdesafio.exception.EntityNotFoundException;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.repository.IntegranteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pelas operações de negócio de {@link Integrante}.
 *
 * <p>Garante que toda lógica fique nesta camada — os controllers apenas
 * delegam a chamada e devolvem a resposta HTTP.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegranteService {

    private final IntegranteRepository integranteRepository;

    // =========================================================================
    // Leitura
    // =========================================================================

    /**
     * Retorna página de integrantes — uso nos endpoints REST paginados.
     *
     * @param pageable parâmetros de paginação e ordenação
     * @return página de {@link IntegranteResponse}
     */
    @Transactional(readOnly = true)
    public Page<IntegranteResponse> listarTodos(Pageable pageable) {
        return integranteRepository.findAll(pageable).map(IntegranteResponse::from);
    }

    /**
     * Retorna todos os integrantes sem paginação — uso nas views Thymeleaf
     * (ex: checkboxes de seleção na tela de montagem de times).
     *
     * @return lista completa de {@link IntegranteResponse}
     */
    @Transactional(readOnly = true)
    public List<IntegranteResponse> listarTodos() {
        return integranteRepository.findAll().stream()
                .map(IntegranteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Busca um integrante pelo id.
     *
     * @param id identificador do integrante
     * @return DTO do integrante encontrado
     * @throws EntityNotFoundException se não existir integrante com o id fornecido
     */
    @Transactional(readOnly = true)
    public IntegranteResponse buscarPorId(Long id) {
        return integranteRepository.findById(id)
                .map(IntegranteResponse::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Integrante não encontrado com id: " + id));
    }

    // =========================================================================
    // Escrita
    // =========================================================================

    /**
     * Cria um novo integrante.
     *
     * @param integrante dados do integrante a criar (id será ignorado)
     * @return DTO do integrante persistido
     */
    @Transactional
    public IntegranteResponse criar(Integrante integrante) {
        integrante.setId(null); // garante inserção
        Integrante salvo = integranteRepository.save(integrante);
        log.info("Integrante criado: id={}, nome={}, funcao={}",
                salvo.getId(), salvo.getNome(), salvo.getFuncao());
        return IntegranteResponse.from(salvo);
    }

    /**
     * Atualiza um integrante existente.
     *
     * @param id         id do integrante a atualizar
     * @param integrante novos dados
     * @return DTO atualizado
     * @throws EntityNotFoundException se não existir integrante com o id fornecido
     */
    @Transactional
    public IntegranteResponse atualizar(Long id, Integrante integrante) {
        if (!integranteRepository.existsById(id)) {
            throw new EntityNotFoundException("Integrante não encontrado com id: " + id);
        }
        integrante.setId(id);
        Integrante salvo = integranteRepository.save(integrante);
        log.info("Integrante atualizado: id={}, nome={}", id, salvo.getNome());
        return IntegranteResponse.from(salvo);
    }

    /**
     * Remove um integrante pelo id.
     *
     * @param id id do integrante a remover
     * @throws EntityNotFoundException se não existir integrante com o id fornecido
     */
    @Transactional
    public void deletar(Long id) {
        if (!integranteRepository.existsById(id)) {
            throw new EntityNotFoundException("Integrante não encontrado com id: " + id);
        }
        integranteRepository.deleteById(id);
        log.info("Integrante removido: id={}", id);
    }
}
