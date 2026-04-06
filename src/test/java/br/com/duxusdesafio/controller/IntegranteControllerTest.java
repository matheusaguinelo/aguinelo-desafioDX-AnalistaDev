package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.repository.IntegranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da camada web para {@link IntegranteController}.
 *
 * <p>Utiliza {@code @WebMvcTest} para carregar apenas a camada MVC e
 * {@code @MockBean} para isolar o repositório, validando o comportamento
 * dos endpoints REST sem acesso ao banco de dados.</p>
 */
@WebMvcTest(IntegranteController.class)
class IntegranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IntegranteRepository integranteRepository;

    private Integrante rogerioCeni;
    private Integrante kaka;

    @BeforeEach
    void setUp() {
        rogerioCeni = Integrante.builder().id(1L).nome("Rogério Ceni").funcao("Goleiro").build();
        kaka        = Integrante.builder().id(2L).nome("Kaká").funcao("Meia").build();
    }

    // =========================================================================
    // GET /api/integrantes
    // =========================================================================

    @Test
    void listarTodos_deveRetornarListaDeIntegrantes() throws Exception {
        when(integranteRepository.findAll()).thenReturn(Arrays.asList(rogerioCeni, kaka));

        mockMvc.perform(get("/api/integrantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Rogério Ceni"))
                .andExpect(jsonPath("$[1].nome").value("Kaká"));
    }

    @Test
    void listarTodos_listaVazia_deveRetornarArrayVazio() throws Exception {
        when(integranteRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/integrantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================================
    // GET /api/integrantes/{id}
    // =========================================================================

    @Test
    void buscarPorId_encontrado_deveRetornar200() throws Exception {
        when(integranteRepository.findById(1L)).thenReturn(Optional.of(rogerioCeni));

        mockMvc.perform(get("/api/integrantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rogério Ceni"))
                .andExpect(jsonPath("$.funcao").value("Goleiro"));
    }

    @Test
    void buscarPorId_naoEncontrado_deveRetornar404() throws Exception {
        when(integranteRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/integrantes/99"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // POST /api/integrantes
    // =========================================================================

    @Test
    void criar_dadosValidos_deveRetornar200ComIntegranteSalvo() throws Exception {
        Integrante novoIntegrante = Integrante.builder().nome("Hernanes").funcao("Volante").build();
        Integrante integranteSalvo = Integrante.builder().id(3L).nome("Hernanes").funcao("Volante").build();

        when(integranteRepository.save(any(Integrante.class))).thenReturn(integranteSalvo);

        mockMvc.perform(post("/api/integrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoIntegrante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.nome").value("Hernanes"))
                .andExpect(jsonPath("$.funcao").value("Volante"));
    }

    // =========================================================================
    // PUT /api/integrantes/{id}
    // =========================================================================

    @Test
    void atualizar_integranteExistente_deveRetornar200() throws Exception {
        Integrante atualizado = Integrante.builder().id(1L).nome("Rogério Ceni").funcao("Goleiro Artilheiro").build();

        when(integranteRepository.existsById(1L)).thenReturn(true);
        when(integranteRepository.save(any(Integrante.class))).thenReturn(atualizado);

        mockMvc.perform(put("/api/integrantes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.funcao").value("Goleiro Artilheiro"));
    }

    @Test
    void atualizar_integranteNaoEncontrado_deveRetornar404() throws Exception {
        when(integranteRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(put("/api/integrantes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rogerioCeni)))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // DELETE /api/integrantes/{id}
    // =========================================================================

    @Test
    void deletar_integranteExistente_deveRetornar204() throws Exception {
        when(integranteRepository.existsById(1L)).thenReturn(true);
        doNothing().when(integranteRepository).deleteById(1L);

        mockMvc.perform(delete("/api/integrantes/1"))
                .andExpect(status().isNoContent());

        verify(integranteRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletar_integranteNaoEncontrado_deveRetornar404() throws Exception {
        when(integranteRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/integrantes/99"))
                .andExpect(status().isNotFound());

        verify(integranteRepository, never()).deleteById(any());
    }
}
