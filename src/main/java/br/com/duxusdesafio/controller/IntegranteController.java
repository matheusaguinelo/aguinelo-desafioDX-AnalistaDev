package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.IntegranteResponse;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.service.IntegranteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;

/**
 * Controller REST para operações CRUD de {@link Integrante}.
 *
 * <p>Delega toda lógica ao {@link IntegranteService} — sem acesso direto ao repositório.</p>
 *
 * <p>Base URL: /api/integrantes</p>
 *
 * <p>Paginação: {@code GET /api/integrantes?page=0&size=20&sort=nome,asc}</p>
 */
@RestController
@RequestMapping("/api/integrantes")
public class IntegranteController {

    private final IntegranteService integranteService;

    public IntegranteController(IntegranteService integranteService) {
        this.integranteService = integranteService;
    }

    /** Lista integrantes com paginação. Padrão: página 0, 20 por página, ordenado por nome. */
    @GetMapping
    public Page<IntegranteResponse> listarTodos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return integranteService.listarTodos(pageable);
    }

    /** Busca um integrante pelo id. Retorna 404 se não encontrado. */
    @GetMapping("/{id}")
    public ResponseEntity<IntegranteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(integranteService.buscarPorId(id));
    }

    /**
     * Cria um novo integrante.
     * Retorna 201 Created com header Location apontando para o recurso criado.
     */
    @PostMapping
    public ResponseEntity<IntegranteResponse> criar(@Valid @RequestBody Integrante integrante) {
        IntegranteResponse salvo = integranteService.criar(integrante);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salvo.getId()).toUri();
        return ResponseEntity.created(location).body(salvo);
    }

    /** Atualiza um integrante existente. Retorna 404 se não encontrado. */
    @PutMapping("/{id}")
    public ResponseEntity<IntegranteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody Integrante integrante) {
        return ResponseEntity.ok(integranteService.atualizar(id, integrante));
    }

    /** Remove um integrante pelo id. Retorna 204 No Content ou 404 se não encontrado. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        integranteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
