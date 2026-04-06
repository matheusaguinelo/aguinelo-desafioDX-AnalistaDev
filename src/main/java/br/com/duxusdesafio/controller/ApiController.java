package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.TimeDaDataResponse;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import br.com.duxusdesafio.repository.TimeRepository;
import br.com.duxusdesafio.service.ApiService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST que expõe os endpoints de processamento/análise de dados.
 * Todos os dados são carregados do banco e processados pelo {@link ApiService}
 * em Java — sem uso de funções de agregação SQL.
 *
 * <p>Base URL: /api</p>
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ApiService apiService;
    private final TimeRepository timeRepository;

    public ApiController(ApiService apiService, TimeRepository timeRepository) {
        this.apiService = apiService;
        this.timeRepository = timeRepository;
    }

    /** Carrega todos os times do banco */
    private List<Time> carregarTodosOsTimes() {
        return timeRepository.findAll();
    }

    /**
     * Retorna o time (clube + integrantes) de uma data específica.
     *
     * <p>Exemplo: {@code GET /api/time-da-data?data=2021-01-15}</p>
     */
    @GetMapping("/time-da-data")
    public ResponseEntity<?> timeDaData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        Time time = apiService.timeDaData(data, carregarTodosOsTimes());

        if (time == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> nomes = time.getComposicaoTimes().stream()
                .map(ct -> ct.getIntegrante().getNome())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new TimeDaDataResponse(time.getData(), time.getNomeClube(), nomes));
    }

    /**
     * Retorna o integrante que apareceu no maior número de times no período.
     *
     * <p>Exemplo: {@code GET /api/integrante-mais-usado?dataInicial=2021-01-01&dataFinal=2021-12-31}</p>
     */
    @GetMapping("/integrante-mais-usado")
    public ResponseEntity<Integrante> integranteMaisUsado(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        Integrante integrante = apiService.integranteMaisUsado(dataInicial, dataFinal, carregarTodosOsTimes());

        return integrante != null
                ? ResponseEntity.ok(integrante)
                : ResponseEntity.notFound().build();
    }

    /**
     * Retorna os nomes dos integrantes do time (clube) mais recorrente no período.
     *
     * <p>Exemplo: {@code GET /api/integrantes-do-time-mais-recorrente}</p>
     */
    @GetMapping("/integrantes-do-time-mais-recorrente")
    public ResponseEntity<List<String>> integrantesDoTimeMaisRecorrente(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        List<String> nomes = apiService.integrantesDoTimeMaisRecorrente(dataInicial, dataFinal, carregarTodosOsTimes());
        return ResponseEntity.ok(nomes);
    }

    /**
     * Retorna a função mais recorrente nos times do período.
     *
     * <p>Exemplo: {@code GET /api/funcao-mais-recorrente}</p>
     */
    @GetMapping("/funcao-mais-recorrente")
    public ResponseEntity<Map<String, String>> funcaoMaisRecorrente(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        String funcao = apiService.funcaoMaisRecorrente(dataInicial, dataFinal, carregarTodosOsTimes());

        if (funcao == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(Collections.singletonMap("Função", funcao));
    }

    /**
     * Retorna o nome do clube mais comum no período.
     *
     * <p>Exemplo: {@code GET /api/clube-mais-recorrente}</p>
     */
    @GetMapping("/clube-mais-recorrente")
    public ResponseEntity<Map<String, String>> clubeMaisRecorrente(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        String clube = apiService.clubeMaisRecorrente(dataInicial, dataFinal, carregarTodosOsTimes());

        if (clube == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(Collections.singletonMap("Clube", clube));
    }

    /**
     * Retorna a contagem de aparições de cada clube no período.
     *
     * <p>Exemplo: {@code GET /api/contagem-de-clubes}</p>
     */
    @GetMapping("/contagem-de-clubes")
    public ResponseEntity<Map<String, Long>> contagemDeClubesNoPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        return ResponseEntity.ok(
                apiService.contagemDeClubesNoPeriodo(dataInicial, dataFinal, carregarTodosOsTimes()));
    }

    /**
     * Retorna a contagem de aparições de cada função nos times do período.
     *
     * <p>Exemplo: {@code GET /api/contagem-por-funcao}</p>
     */
    @GetMapping("/contagem-por-funcao")
    public ResponseEntity<Map<String, Long>> contagemPorFuncao(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {

        return ResponseEntity.ok(
                apiService.contagemPorFuncao(dataInicial, dataFinal, carregarTodosOsTimes()));
    }
}
