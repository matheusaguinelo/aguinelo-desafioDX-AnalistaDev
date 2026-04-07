package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.dto.TimeResponse;
import br.com.duxusdesafio.service.TimeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;

/**
 * Controller REST para operações CRUD de {@link br.com.duxusdesafio.model.Time}.
 *
 * <p>Delega toda lógica ao {@link TimeService}, incluindo as regras de negócio
 * da criação (clube duplicado na mesma data, integrantes duplicados).</p>
 *
 * <p>Base URL: /api/times</p>
 *
 * <p>Paginação: {@code GET /api/times?page=0&size=20&sort=data,desc}</p>
 */
@RestController
@RequestMapping("/api/times")
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    /** Lista times com paginação. Padrão: página 0, 20 por página, ordenado por data desc. */
    @GetMapping
    public Page<TimeResponse> listarTodos(
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        return timeService.listarTodos(pageable);
    }

    /** Busca um time pelo id. Retorna 404 se não encontrado. */
    @GetMapping("/{id}")
    public ResponseEntity<TimeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(timeService.buscarPorId(id));
    }

    /**
     * Cria um novo time e vincula os integrantes informados.
     * Retorna 201 Created com header Location apontando para o recurso criado.
     *
     * @param request DTO com nomeClube, data e lista de integranteIds
     */
    @PostMapping
    public ResponseEntity<TimeResponse> criar(@Valid @RequestBody TimeRequest request) {
        TimeResponse salvo = timeService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(salvo);
    }

    /** Remove um time pelo id. Retorna 204 No Content ou 404 se não encontrado. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        timeService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
