package br.com.duxusdesafio;

import br.com.duxusdesafio.dto.IntegranteResponse;
import br.com.duxusdesafio.dto.TimeResponse;
import br.com.duxusdesafio.dto.TimeDaDataResponse;
import br.com.duxusdesafio.dto.TimeRequest;
import br.com.duxusdesafio.model.Integrante;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de integração end-to-end: sobe o contexto completo do Spring Boot
 * com banco H2 real (profile "test") e valida o fluxo completo via HTTP.
 *
 * <p>Ordem dos testes garante que os dados criados em passos anteriores
 * estejam disponíveis para os passos seguintes.</p>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegracaoApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // IDs compartilhados entre os testes (estado de classe)
    private static Long integranteId;
    private static Long timeId;

    // =========================================================================
    // Passo 1 — Cadastrar integrante
    // =========================================================================

    @Test
    @Order(1)
    void deveCriarIntegranteERetornar201() {
        Integrante integrante = new Integrante();
        integrante.setNome("Hernanes");
        integrante.setFuncao("Volante");

        ResponseEntity<IntegranteResponse> resposta =
                restTemplate.postForEntity("/api/integrantes", integrante, IntegranteResponse.class);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertNotNull(resposta.getBody().getId());
        assertEquals("Hernanes", resposta.getBody().getNome());
        assertEquals("Volante", resposta.getBody().getFuncao());
        assertNotNull(resposta.getHeaders().getLocation(), "Header Location deve estar presente");

        integranteId = resposta.getBody().getId();
    }

    // =========================================================================
    // Passo 2 — Buscar integrante por id
    // =========================================================================

    @Test
    @Order(2)
    void deveBuscarIntegrantePorId() {
        ResponseEntity<IntegranteResponse> resposta =
                restTemplate.getForEntity("/api/integrantes/" + integranteId, IntegranteResponse.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Hernanes", resposta.getBody().getNome());
    }

    // =========================================================================
    // Passo 3 — Cadastrar time com o integrante criado
    // =========================================================================

    @Test
    @Order(3)
    void deveCriarTimeERetornar201() {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Collections.singletonList(integranteId));

        ResponseEntity<TimeResponse> resposta =
                restTemplate.postForEntity("/api/times", request, TimeResponse.class);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("São Paulo", resposta.getBody().getNomeClube());
        assertTrue(resposta.getBody().getIntegrantes().contains("Hernanes"));
        assertNotNull(resposta.getHeaders().getLocation(), "Header Location deve estar presente");

        timeId = resposta.getBody().getId();
    }

    // =========================================================================
    // Passo 4 — Regra de negócio: mesmo clube + mesma data → 422
    // =========================================================================

    @Test
    @Order(4)
    void deveRejeitarClubeDuplicadoNaMesmaData() {
        TimeRequest request = new TimeRequest();
        request.setNomeClube("São Paulo");
        request.setData(LocalDate.of(2024, 1, 7));
        request.setIntegranteIds(Collections.singletonList(integranteId));

        ResponseEntity<String> resposta =
                restTemplate.postForEntity("/api/times", request, String.class);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
    }

    // =========================================================================
    // Passo 5 — Consultar time da data
    // =========================================================================

    @Test
    @Order(5)
    void deveConsultarTimeDaData() {
        ResponseEntity<TimeDaDataResponse> resposta = restTemplate.getForEntity(
                "/api/time-da-data?data=2024-01-07", TimeDaDataResponse.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody());
        assertEquals("São Paulo", resposta.getBody().getClube());
        assertTrue(resposta.getBody().getIntegrantes().contains("Hernanes"));
    }

    // =========================================================================
    // Passo 6 — Consultar integrante mais usado
    // =========================================================================

    @Test
    @Order(6)
    void deveRetornarIntegranteMaisUsado() {
        ResponseEntity<Integrante> resposta =
                restTemplate.getForEntity("/api/integrante-mais-usado", Integrante.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals("Hernanes", resposta.getBody().getNome());
    }

    // =========================================================================
    // Passo 7 — Remover time e integrante (limpeza)
    // =========================================================================

    @Test
    @Order(7)
    void deveRemoverTimeERetornar204() {
        restTemplate.delete("/api/times/" + timeId);

        ResponseEntity<String> resposta =
                restTemplate.getForEntity("/api/times/" + timeId, String.class);
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    @Test
    @Order(8)
    void deveRemoverIntegranteERetornar204() {
        restTemplate.delete("/api/integrantes/" + integranteId);

        ResponseEntity<String> resposta =
                restTemplate.getForEntity("/api/integrantes/" + integranteId, String.class);
        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }
}
