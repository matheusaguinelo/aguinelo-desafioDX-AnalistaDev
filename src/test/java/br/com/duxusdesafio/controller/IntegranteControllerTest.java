package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.dto.IntegranteResponse;
import br.com.duxusdesafio.exception.EntityNotFoundException;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.service.IntegranteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link IntegranteController}.
 *
 * <p>Utiliza {@code @WebMvcTest} para carregar apenas a camada MVC e
 * {@code @MockBean} para isolar o service, validando os endpoints REST
 * sem acesso ao banco de dados.</p>
 */
@WebMvcTest(IntegranteController.class)
class IntegranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntegranteService integranteService;

    private IntegranteResponse rogerioCeniResponse;
    private IntegranteResponse kakaResponse;

    @BeforeEach
    void setUp() {
        rogerioCeniResponse = new IntegranteResponse(1L, "Rogério Ceni", "Goleiro");
        kakaResponse        = new IntegranteResponse(2L, "Kaká", "Meia");
    }

    // =========================================================================
    // GET /api/integrantes (paginado)
    // =========================================================================

    @Test
    void listarTodos_deveRetornarPageDeIntegrantes() throws Exception {
        when(integranteService.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(rogerioCeniResponse, kakaResponse)));

        mockMvc.perform(get("/api/integrantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nome").value("Rogério Ceni"))
                .andExpect(jsonPath("$.content[1].nome").value("Kaká"));
    }

    @Test
    void listarTodos_listaVazia_deveRetornarPageVazia() throws Exception {
        when(integranteService.listarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/integrantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // =========================================================================
    // GET /api/integrantes/{id}
    // =========================================================================

    @Test
    void buscarPorId_encontrado_deveRetornar200() throws Exception {
        when(integranteService.buscarPorId(1L)).thenReturn(rogerioCeniResponse);

        mockMvc.perform(get("/api/integrantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rogério Ceni"))
                .andExpect(jsonPath("$.funcao").value("Goleiro"));
    }

    @Test
    void buscarPorId_naoEncontrado_deveRetornar404() throws Exception {
        when(integranteService.buscarPorId(99L))
                .thenThrow(new EntityNotFoundException("Integrante não encontrado com id: 99"));

        mockMvc.perform(get("/api/integrantes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));
    }

    // =========================================================================
    // POST /api/integrantes
    // =========================================================================

    @Test
    void criar_dadosValidos_deveRetornar201ComIntegranteSalvo() throws Exception {
        Integrante novoIntegrante = Integrante.builder().nome("Hernanes").funcao("Volante").build();
        IntegranteResponse salvo  = new IntegranteResponse(3L, "Hernanes", "Volante");

        when(integranteService.criar(any(Integrante.class))).thenReturn(salvo);

        mockMvc.perform(post("/api/integrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoIntegrante)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Hernanes"))
                .andExpect(jsonPath("$.funcao").value("Volante"));
    }

    // =========================================================================
    // PUT /api/integrantes/{id}
    // =========================================================================

    @Test
    void atualizar_integranteExistente_deveRetornar200() throws Exception {
        Integrante atualizado       = Integrante.builder().nome("Rogério Ceni").funcao("Goleiro Artilheiro").build();
        IntegranteResponse response = new IntegranteResponse(1L, "Rogério Ceni", "Goleiro Artilheiro");

        when(integranteService.atualizar(eq(1L), any(Integrante.class))).thenReturn(response);

        mockMvc.perform(put("/api/integrantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcao").value("Goleiro Artilheiro"));
    }

    @Test
    void atualizar_integranteNaoEncontrado_deveRetornar404() throws Exception {
        Integrante atualizado = Integrante.builder().nome("X").funcao("Y").build();

        when(integranteService.atualizar(eq(99L), any(Integrante.class)))
                .thenThrow(new EntityNotFoundException("Integrante não encontrado com id: 99"));

        mockMvc.perform(put("/api/integrantes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));
    }

    // =========================================================================
    // DELETE /api/integrantes/{id}
    // =========================================================================

    @Test
    void deletar_integranteExistente_deveRetornar204() throws Exception {
        doNothing().when(integranteService).deletar(1L);

        mockMvc.perform(delete("/api/integrantes/1"))
                .andExpect(status().isNoContent());

        verify(integranteService, times(1)).deletar(1L);
    }

    @Test
    void deletar_integranteNaoEncontrado_deveRetornar404() throws Exception {
        doThrow(new EntityNotFoundException("Integrante não encontrado com id: 99"))
                .when(integranteService).deletar(99L);

        mockMvc.perform(delete("/api/integrantes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NOT_FOUND"));

        verify(integranteService, times(1)).deletar(99L);
    }
}
