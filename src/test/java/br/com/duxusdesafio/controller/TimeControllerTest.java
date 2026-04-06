package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import br.com.duxusdesafio.repository.IntegranteRepository;
import br.com.duxusdesafio.repository.TimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link TimeController}.
 *
 * <p>Valida a criação de times com composição de jogadores, garantindo
 * que o controller orquestra corretamente os repositórios de Time e Integrante.</p>
 */
@WebMvcTest(TimeController.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeRepository timeRepository;

    @MockBean
    private IntegranteRepository integranteRepository;

    private Integrante hernanes;
    private Integrante kaka;
    private Time timeSaoPaulo;

    @BeforeEach
    void setUp() {
        // Configura suporte a LocalDate no ObjectMapper
        objectMapper.registerModule(new JavaTimeModule());

        hernanes = Integrante.builder().id(1L).nome("Hernanes").funcao("Volante").build();
        kaka     = Integrante.builder().id(2L).nome("Kaká").funcao("Meia").build();

        timeSaoPaulo = Time.builder()
                .id(1L)
                .nomeClube("São Paulo")
                .data(LocalDate.of(2024, 1, 7))
                .build();

        ComposicaoTime ct1 = ComposicaoTime.builder().id(1L).time(timeSaoPaulo).integrante(hernanes).build();
        ComposicaoTime ct2 = ComposicaoTime.builder().id(2L).time(timeSaoPaulo).integrante(kaka).build();
        timeSaoPaulo.getComposicaoTimes().addAll(Arrays.asList(ct1, ct2));
    }

    // =========================================================================
    // GET /api/times
    // =========================================================================

    @Test
    void listarTodos_deveRetornarListaDeTimes() throws Exception {
        when(timeRepository.findAll()).thenReturn(Collections.singletonList(timeSaoPaulo));

        mockMvc.perform(get("/api/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nomeClube").value("São Paulo"));
    }

    @Test
    void listarTodos_listaVazia_deveRetornarArrayVazio() throws Exception {
        when(timeRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // GET /api/times/{id}
    // =========================================================================

    @Test
    void buscarPorId_encontrado_deveRetornar200() throws Exception {
        when(timeRepository.findById(1L)).thenReturn(Optional.of(timeSaoPaulo));

        mockMvc.perform(get("/api/times/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeClube").value("São Paulo"))
                .andExpect(jsonPath("$.data").value("2024-01-07"));
    }

    @Test
    void buscarPorId_naoEncontrado_deveRetornar404() throws Exception {
        when(timeRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/times/99"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // POST /api/times
    // =========================================================================

    @Test
    void criar_dadosValidos_deveRetornarTimeSalvo() throws Exception {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Arrays.asList(1L, 2L));

        List<Integrante> integrantes = Arrays.asList(hernanes, kaka);
        when(integranteRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(integrantes);
        when(timeRepository.save(any(Time.class))).thenReturn(timeSaoPaulo);

        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeClube").value("São Paulo"));
    }

    @Test
    void criar_integranteNaoEncontrado_deveRetornar400() throws Exception {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Arrays.asList(1L, 999L)); // id 999 não existe

        // Repositório retorna apenas 1 dos 2 integrantes solicitados
        when(integranteRepository.findAllById(any())).thenReturn(Collections.singletonList(hernanes));

        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // DELETE /api/times/{id}
    // =========================================================================

    @Test
    void deletar_timeExistente_deveRetornar204() throws Exception {
        when(timeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(timeRepository).deleteById(1L);

        mockMvc.perform(delete("/api/times/1"))
                .andExpect(status().isNoContent());

        verify(timeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletar_timeNaoEncontrado_deveRetornar404() throws Exception {
        when(timeRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/times/99"))
                .andExpect(status().isNotFound());

        verify(timeRepository, never()).deleteById(any());
    }
}
