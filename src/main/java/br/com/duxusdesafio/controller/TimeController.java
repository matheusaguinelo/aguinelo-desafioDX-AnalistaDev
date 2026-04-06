package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import br.com.duxusdesafio.repository.IntegranteRepository;
import br.com.duxusdesafio.repository.TimeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para operações CRUD de {@link Time}.
 * A criação de um time já vincula os integrantes via {@link ComposicaoTime}.
 * Base URL: /api/times
 */
@RestController
@RequestMapping("/api/times")
public class TimeController {

    private final TimeRepository timeRepository;
    private final IntegranteRepository integranteRepository;

    public TimeController(TimeRepository timeRepository, IntegranteRepository integranteRepository) {
        this.timeRepository = timeRepository;
        this.integranteRepository = integranteRepository;
    }

    /** Lista todos os times com suas composições. */
    @GetMapping
    public List<Time> listarTodos() {
        return timeRepository.findAll();
    }

    /** Busca um time pelo id. */
    @GetMapping("/{id}")
    public ResponseEntity<Time> buscarPorId(@PathVariable Long id) {
        return timeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cria um novo time e vincula os integrantes informados.
     *
     * @param request DTO com nomeClube, data e lista de integranteIds
     * @return time criado com composição
     */
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody TimeRequest request) {
        // Valida e carrega os integrantes
        List<Integrante> integrantes = integranteRepository.findAllById(request.getIntegranteIds());

        if (integrantes.size() != request.getIntegranteIds().size()) {
            return ResponseEntity.badRequest()
                    .body("Um ou mais integrantes não foram encontrados.");
        }

        Time time = Time.builder()
                .nomeClube(request.getNomeClube())
                .data(request.getData())
                .build();

        // Cria os vínculos ComposicaoTime para cada integrante
        List<ComposicaoTime> composicoes = integrantes.stream()
                .map(i -> ComposicaoTime.builder()
                        .time(time)
                        .integrante(i)
                        .build())
                .collect(Collectors.toList());

        time.getComposicaoTimes().addAll(composicoes);

        Time salvo = timeRepository.save(time);
        return ResponseEntity.ok(salvo);
    }

    /** Remove um time pelo id. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!timeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
