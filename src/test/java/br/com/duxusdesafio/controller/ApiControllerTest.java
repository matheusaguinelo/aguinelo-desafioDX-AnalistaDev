package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import br.com.duxusdesafio.repository.TimeRepository;
import br.com.duxusdesafio.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link ApiController}.
 *
 * <p>Valida os 7 endpoints de análise de dados, garantindo que o controller
 * delega corretamente ao {@link ApiService} e retorna as respostas no
 * formato esperado, sem acessar o banco de dados.</p>
 *
 * <p>O {@link TimeRepository} é mockado para simular o carregamento de todos
 * os times, e o {@link ApiService} é mockado para isolar a lógica de negócio.</p>
 */
@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiService apiService;

    @MockBean
    private TimeRepository timeRepository;

    private Integrante hernanes;
    private Integrante kaka;
    private Integrante rogerioCeni;
    private Time timeSP;
    private List<Time> todosOsTimes;

    @BeforeEach
    void setUp() {
        hernanes    = Integrante.builder().id(1L).nome("Hernanes").funcao("Volante").build();
        kaka        = Integrante.builder().id(2L).nome("Kaká").funcao("Meia").build();
        rogerioCeni = Integrante.builder().id(3L).nome("Rogério Ceni").funcao("Goleiro").build();

        timeSP = Time.builder()
                .id(1L)
                .nomeClube("São Paulo")
                .data(LocalDate.of(2024, 1, 7))
                .build();

        ComposicaoTime ct1 = ComposicaoTime.builder().id(1L).time(timeSP).integrante(hernanes).build();
        ComposicaoTime ct2 = ComposicaoTime.builder().id(2L).time(timeSP).integrante(kaka).build();
        timeSP.getComposicaoTimes().addAll(Arrays.asList(ct1, ct2));

        todosOsTimes = Collections.singletonList(timeSP);
        when(timeRepository.findAll()).thenReturn(todosOsTimes);
    }

    // =========================================================================
    // GET /api/time-da-data
    // =========================================================================

    @Test
    void timeDaData_encontrado_deveRetornarDadosDoTime() throws Exception {
        when(apiService.timeDaData(any(LocalDate.class), anyList())).thenReturn(timeSP);

        mockMvc.perform(get("/api/time-da-data").param("data", "2024-01-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clube").value("São Paulo"))
                .andExpect(jsonPath("$.data").value("2024-01-07"))
                .andExpect(jsonPath("$.integrantes").isArray())
                .andExpect(jsonPath("$.integrantes[0]").value("Hernanes"));
    }

    @Test
    void timeDaData_naoEncontrado_deveRetornar404() throws Exception {
        when(apiService.timeDaData(any(LocalDate.class), anyList())).thenReturn(null);

        mockMvc.perform(get("/api/time-da-data").param("data", "2099-01-01"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /api/integrante-mais-usado
    // =========================================================================

    @Test
    void integranteMaisUsado_encontrado_deveRetornarIntegrante() throws Exception {
        when(apiService.integranteMaisUsado(any(), any(), anyList())).thenReturn(hernanes);

        mockMvc.perform(get("/api/integrante-mais-usado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Hernanes"))
                .andExpect(jsonPath("$.funcao").value("Volante"));
    }

    @Test
    void integranteMaisUsado_comFiltroDeData_devePassarDatasAoService() throws Exception {
        when(apiService.integranteMaisUsado(
                eq(LocalDate.of(2024, 1, 1)),
                eq(LocalDate.of(2024, 1, 31)),
                anyList()
        )).thenReturn(hernanes);

        mockMvc.perform(get("/api/integrante-mais-usado")
                        .param("dataInicial", "2024-01-01")
                        .param("dataFinal", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Hernanes"));
    }

    @Test
    void integranteMaisUsado_naoEncontrado_deveRetornar404() throws Exception {
        when(apiService.integranteMaisUsado(any(), any(), anyList())).thenReturn(null);

        mockMvc.perform(get("/api/integrante-mais-usado"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /api/integrantes-do-time-mais-recorrente
    // =========================================================================

    @Test
    void integrantesDoTimeMaisRecorrente_deveRetornarListaDeNomes() throws Exception {
        when(apiService.integrantesDoTimeMaisRecorrente(any(), any(), anyList()))
                .thenReturn(Arrays.asList("Hernanes", "Kaká"));

        mockMvc.perform(get("/api/integrantes-do-time-mais-recorrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Hernanes"))
                .andExpect(jsonPath("$[1]").value("Kaká"));
    }

    @Test
    void integrantesDoTimeMaisRecorrente_listaVazia_deveRetornarArrayVazio() throws Exception {
        when(apiService.integrantesDoTimeMaisRecorrente(any(), any(), anyList()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/integrantes-do-time-mais-recorrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // GET /api/funcao-mais-recorrente
    // =========================================================================

    @Test
    void funcaoMaisRecorrente_encontrada_deveRetornarFuncao() throws Exception {
        when(apiService.funcaoMaisRecorrente(any(), any(), anyList())).thenReturn("Volante");

        mockMvc.perform(get("/api/funcao-mais-recorrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Função").value("Volante"));
    }

    @Test
    void funcaoMaisRecorrente_naoEncontrada_deveRetornar404() throws Exception {
        when(apiService.funcaoMaisRecorrente(any(), any(), anyList())).thenReturn(null);

        mockMvc.perform(get("/api/funcao-mais-recorrente"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /api/clube-mais-recorrente
    // =========================================================================

    @Test
    void clubeMaisRecorrente_encontrado_deveRetornarClube() throws Exception {
        when(apiService.clubeMaisRecorrente(any(), any(), anyList())).thenReturn("São Paulo");

        mockMvc.perform(get("/api/clube-mais-recorrente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Clube").value("São Paulo"));
    }

    @Test
    void clubeMaisRecorrente_naoEncontrado_deveRetornar404() throws Exception {
        when(apiService.clubeMaisRecorrente(any(), any(), anyList())).thenReturn(null);

        mockMvc.perform(get("/api/clube-mais-recorrente"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // GET /api/contagem-de-clubes
    // =========================================================================

    @Test
    void contagemDeClubesNoPeriodo_deveRetornarMapaDeContagens() throws Exception {
        Map<String, Long> contagem = new LinkedHashMap<>();
        contagem.put("São Paulo", 3L);
        contagem.put("Palmeiras", 1L);
        contagem.put("Santos", 1L);

        when(apiService.contagemDeClubesNoPeriodo(any(), any(), anyList())).thenReturn(contagem);

        mockMvc.perform(get("/api/contagem-de-clubes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['São Paulo']").value(3))
                .andExpect(jsonPath("$.Palmeiras").value(1))
                .andExpect(jsonPath("$.Santos").value(1));
    }

    // =========================================================================
    // GET /api/contagem-por-funcao
    // =========================================================================

    @Test
    void contagemPorFuncao_deveRetornarMapaDeContagens() throws Exception {
        Map<String, Long> contagem = new LinkedHashMap<>();
        contagem.put("Volante", 4L);
        contagem.put("Meia", 3L);
        contagem.put("Atacante", 3L);
        contagem.put("Goleiro", 1L);

        when(apiService.contagemPorFuncao(any(), any(), anyList())).thenReturn(contagem);

        mockMvc.perform(get("/api/contagem-por-funcao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Volante").value(4))
                .andExpect(jsonPath("$.Meia").value(3))
                .andExpect(jsonPath("$.Atacante").value(3))
                .andExpect(jsonPath("$.Goleiro").value(1));
    }
}
