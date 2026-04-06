package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.repository.IntegranteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * Controller REST para operações CRUD de {@link Integrante}.
 * Base URL: /api/integrantes
 */
@RestController
@RequestMapping("/api/integrantes")
public class IntegranteController {

    private final IntegranteRepository integranteRepository;

    public IntegranteController(IntegranteRepository integranteRepository) {
        this.integranteRepository = integranteRepository;
    }

    /** Lista todos os integrantes cadastrados. */
    @GetMapping
    public List<Integrante> listarTodos() {
        return integranteRepository.findAll();
    }

    /** Busca um integrante pelo id. */
    @GetMapping("/{id}")
    public ResponseEntity<Integrante> buscarPorId(@PathVariable Long id) {
        return integranteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Cria um novo integrante. */
    @PostMapping
    public ResponseEntity<Integrante> criar(@Valid @RequestBody Integrante integrante) {
        integrante.setId(null); // garante que é uma inserção
        Integrante salvo = integranteRepository.save(integrante);
        return ResponseEntity.ok(salvo);
    }

    /** Atualiza um integrante existente. */
    @PutMapping("/{id}")
    public ResponseEntity<Integrante> atualizar(@PathVariable Long id,
                                                @Valid @RequestBody Integrante integrante) {
        if (!integranteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        integrante.setId(id);
        return ResponseEntity.ok(integranteRepository.save(integrante));
    }

    /** Remove um integrante pelo id. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!integranteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        integranteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
