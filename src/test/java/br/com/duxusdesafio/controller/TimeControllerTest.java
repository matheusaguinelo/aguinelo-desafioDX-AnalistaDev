package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.dto.TimeResponse;
import br.com.duxusdesafio.exception.BusinessRuleException;
import br.com.duxusdesafio.exception.EntityNotFoundException;
import br.com.duxusdesafio.service.TimeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link TimeController}.
 *
 * <p>Valida a criação de times, paginação e tratamento de erros de negócio,
 * garantindo que o controller delega ao {@link TimeService} corretamente.</p>
 */
@WebMvcTest(TimeController.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TimeService timeService;

    private TimeResponse timeSaoPauloResponse;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        timeSaoPauloResponse = new TimeResponse(
                1L, "São Paulo", LocalDate.of(2024, 1, 7),
                Arrays.asList("Hernanes", "Kaká"));
    }

    // =========================================================================
    // GET /api/times (paginado)
    // =========================================================================

    @Test
    void listarTodos_deveRetornarPageDeTimes() throws Exception {
        when(timeService.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(timeSaoPauloResponse)));

        mockMvc.perform(get("/api/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeClube").value("São Paulo"))
                .andExpect(jsonPath("$.content[0].integrantes[0]").value("Hernanes"));
    }

    @Test
    void listarTodos_listaVazia_deveRetornarPageVazia() throws Exception {
        when(timeService.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/times"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // =========================================================================
    // GET /api/times/{id}
    // =========================================================================

    @Test
    void buscarPorId_encontrado_deveRetornar200() throws Exception {
        when(timeService.buscarPorId(1L)).thenReturn(timeSaoPauloResponse);

        mockMvc.perform(get("/api/times/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeClube").value("São Paulo"))
                .andExpect(jsonPath("$.data").value("2024-01-07"));
    }

    @Test
    void buscarPorId_naoEncontrado_deveRetornar404() throws Exception {
        when(timeService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException("Time não encontrado com id: 99"));

        mockMvc.perform(get("/api/times/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));
    }

    // =========================================================================
    // POST /api/times
    // =========================================================================

    @Test
    void criar_dadosValidos_deveRetornar201ComTimeSalvo() throws Exception {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Arrays.asList(1L, 2L));

        when(timeService.criar(any(TimeRequest.class))).thenReturn(timeSaoPauloResponse);

        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nomeClube").value("São Paulo"))
                .andExpect(jsonPath("$.integrantes[0]").value("Hernanes"));
    }

    @Test
    void criar_clubeDuplicadoNaData_deveRetornar422() throws Exception {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Arrays.asList(1L, 2L));

        when(timeService.criar(any(TimeRequest.class)))
                .thenThrow(new BusinessRuleException(
                        "O clube 'São Paulo' já possui um time cadastrado para 2024-01-07."));

        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void criar_integranteNaoEncontrado_deveRetornar404() throws Exception {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Arrays.asList(1L, 999L));

        when(timeService.criar(any(TimeRequest.class)))
                .thenThrow(new EntityNotFoundException("Um ou mais integrantes informados não foram encontrados."));

        mockMvc.perform(post("/api/times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));
    }

    // =========================================================================
    // DELETE /api/times/{id}
    // =========================================================================

    @Test
    void deletar_timeExistente_deveRetornar204() throws Exception {
        doNothing().when(timeService).deletar(1L);

        mockMvc.perform(delete("/api/times/1"))
                .andExpect(status().isNoContent());

        verify(timeService, times(1)).deletar(1L);
    }

    @Test
    void deletar_timeNaoEncontrado_deveRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("Time não encontrado com id: 99"))
                .when(timeService).deletar(eq(99L));

        mockMvc.perform(delete("/api/times/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));

        verify(timeService, times(1)).deletar(99L);
    }
}
