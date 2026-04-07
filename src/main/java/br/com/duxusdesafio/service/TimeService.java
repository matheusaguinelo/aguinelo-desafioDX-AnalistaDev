package br.com.duxusdesafio.service;

import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.dto.TimeResponse;
import br.com.duxusdesafio.exception.BusinessRuleException;
import br.com.duxusdesafio.exception.EntityNotFoundException;
import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import br.com.duxusdesafio.repository.IntegranteRepository;
import br.com.duxusdesafio.repository.TimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsável pelas operações de negócio de {@link Time}.
 *
 * <p><b>Regras de negócio aplicadas na criação:</b></p>
 * <ul>
 *   <li>Mesmo clube não pode ter dois times cadastrados na mesma data</li>
 *   <li>Um time não pode ter integrantes duplicados</li>
 *   <li>Todos os integrantes informados devem existir no banco</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;
    private final IntegranteRepository integranteRepository;

    // =========================================================================
    // Leitura
    // =========================================================================

    /**
     * Retorna página de times — uso nos endpoints REST paginados.
     * As coleções de composição são carregadas dentro do contexto transacional
     * com batching via {@code @BatchSize}, evitando N+1 queries.
     *
     * @param pageable parâmetros de paginação e ordenação
     * @return página de {@link TimeResponse}
     */
    @Transactional(readOnly = true)
    public Page<TimeResponse> listarTodos(Pageable pageable) {
        return timeRepository.findAll(pageable).map(TimeResponse::from);
    }

    /**
     * Retorna todos os times com composição — uso nas views Thymeleaf.
     *
     * @return lista completa de {@link TimeResponse}
     */
    @Transactional(readOnly = true)
    public List<TimeResponse> listarTodosParaView() {
        return timeRepository.findAllWithComposicao().stream()
                .map(TimeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos os times com composição completa — uso no {@link ApiService}
     * para processamento em Java (sem agregações SQL).
     *
     * @return lista de entidades {@link Time} com composicaoTimes carregados
     */
    @Transactional(readOnly = true)
    public List<Time> listarTodosParaProcessamento() {
        return timeRepository.findAllWithComposicao();
    }

    /**
     * Busca um time pelo id com composição completa.
     *
     * @param id identificador do time
     * @return DTO do time encontrado
     * @throws EntityNotFoundException se não existir time com o id fornecido
     */
    @Transactional(readOnly = true)
    public TimeResponse buscarPorId(Long id) {
        return timeRepository.findById(id)
                .map(TimeResponse::from)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Time não encontrado com id: " + id));
    }

    // =========================================================================
    // Escrita
    // =========================================================================

    /**
     * Cria um novo time e vincula os integrantes informados.
     *
     * @param request DTO com nomeClube, data e lista de integranteIds
     * @return DTO do time persistido
     * @throws BusinessRuleException   se o clube já tiver time na data ou houver integrantes duplicados
     * @throws EntityNotFoundException se algum integranteId não existir no banco
     */
    @Transactional
    public TimeResponse criar(TimeRequest request) {
        // Regra 1: mesmo clube não pode ter dois times na mesma data
        if (timeRepository.existsByNomeClubeAndData(request.getNomeClube(), request.getData())) {
            throw new BusinessRuleException(
                    "O clube '" + request.getNomeClube() + "' já possui um time cadastrado para "
                            + request.getData() + ".");
        }

        // Regra 2: sem integrantes duplicados na lista de ids
        long qtdDistintos = request.getIntegranteIds().stream().distinct().count();
        if (qtdDistintos != request.getIntegranteIds().size()) {
            throw new BusinessRuleException("O time não pode conter integrantes duplicados.");
        }

        // Valida e carrega os integrantes
        List<Integrante> integrantes = integranteRepository.findAllById(request.getIntegranteIds());
        if (integrantes.size() != request.getIntegranteIds().size()) {
            throw new EntityNotFoundException("Um ou mais integrantes informados não foram encontrados.");
        }

        Time time = Time.builder()
                .nomeClube(request.getNomeClube())
                .data(request.getData())
                .build();

        List<ComposicaoTime> composicoes = integrantes.stream()
                .map(i -> ComposicaoTime.builder().time(time).integrante(i).build())
                .collect(Collectors.toList());

        time.getComposicaoTimes().addAll(composicoes);

        Time salvo = timeRepository.save(time);
        log.info("Time criado: id={}, clube={}, data={}, {} integrante(s)",
                salvo.getId(), salvo.getNomeClube(), salvo.getData(), integrantes.size());
        return TimeResponse.from(salvo);
    }

    /**
     * Remove um time pelo id.
     *
     * @param id id do time a remover
     * @throws EntityNotFoundException se não existir time com o id fornecido
     */
    @Transactional
    public void deletar(Long id) {
        if (!timeRepository.existsById(id)) {
            throw new EntityNotFoundException("Time não encontrado com id: " + id);
        }
        timeRepository.deleteById(id);
        log.info("Time removido: id={}", id);
    }
}
