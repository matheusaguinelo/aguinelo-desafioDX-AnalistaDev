package br.com.duxusdesafio.service;

import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Testes unitários para {@link ApiService}.
 *
 * <p>Todos os testes usam objetos criados em memória, sem necessidade de banco de dados,
 * validando exclusivamente a lógica de processamento em Java.</p>
 *
 * <p>Cenário base inspirado no futebol brasileiro com ídolos do São Paulo FC:</p>
 * <ul>
 *   <li>5 jogadores históricos do São Paulo FC com funções distintas</li>
 *   <li>5 times escalados em datas distintas: São Paulo (3x), Palmeiras (1x), Santos (1x)</li>
 *   <li>São Paulo  → clube mais recorrente (3 aparições)</li>
 *   <li>Hernanes   → jogador mais usado (4 aparições)</li>
 *   <li>Volante    → função mais recorrente (4 aparições)</li>
 * </ul>
 */
public class ApiServiceTest {

    private ApiService apiService;

    // -------------------------------------------------------------------------
    // Ídolos do São Paulo FC reutilizados nos testes
    // -------------------------------------------------------------------------
    private Integrante rogerioCeni;  // Goleiro  (id=1) – lenda máxima do São Paulo
    private Integrante kaka;         // Meia     (id=2) – Ballon d'Or 2007
    private Integrante lucasMoura;   // Atacante (id=3) – velocidade e dribles
    private Integrante hernanes;     // Volante  (id=4) – "A Bíblia"
    private Integrante calleri;      // Atacante (id=5) – artilheiro argentino

    // -------------------------------------------------------------------------
    // Times (escalações semanais) reutilizados nos testes
    // -------------------------------------------------------------------------
    private Time timeSP1;      // São Paulo – 2024-01-07
    private Time timeSP2;      // São Paulo – 2024-01-14
    private Time timeSP3;      // São Paulo – 2024-01-21
    private Time timePalm;     // Palmeiras – 2024-01-07
    private Time timeSantos;   // Santos    – 2024-02-01

    private List<Time> todosOsTimes;

    @Before
    public void setUp() {
        apiService = new ApiService();

        // Jogadores do São Paulo FC
        rogerioCeni = criarIntegrante(1L, "Rogério Ceni", "Goleiro");
        kaka        = criarIntegrante(2L, "Kaká",         "Meia");
        lucasMoura  = criarIntegrante(3L, "Lucas Moura",  "Atacante");
        hernanes    = criarIntegrante(4L, "Hernanes",     "Volante");
        calleri     = criarIntegrante(5L, "Calleri",      "Atacante");

        // Escalações semanais
        // São Paulo aparece 3x → clube mais recorrente
        // Hernanes aparece em 4 times → jogador mais usado
        // Volante aparece 4x → função mais recorrente
        timeSP1    = criarTime(1L, "São Paulo", LocalDate.of(2024, 1,  7), lucasMoura, hernanes, kaka);
        timeSP2    = criarTime(2L, "São Paulo", LocalDate.of(2024, 1, 14), hernanes, kaka);
        timeSP3    = criarTime(3L, "São Paulo", LocalDate.of(2024, 1, 21), hernanes, calleri);
        timePalm   = criarTime(4L, "Palmeiras", LocalDate.of(2024, 1,  7), hernanes, rogerioCeni);
        timeSantos = criarTime(5L, "Santos",    LocalDate.of(2024, 2,  1), calleri, kaka);

        todosOsTimes = Arrays.asList(timeSP1, timeSP2, timeSP3, timePalm, timeSantos);
    }

    // =========================================================================
    // timeDaData
    // =========================================================================

    @Test
    public void timeDaData_deveRetornarTimeCorreto() {
        // Existem dois times em 07/01/2024 (São Paulo e Palmeiras); basta não ser null
        Time resultado = apiService.timeDaData(LocalDate.of(2024, 1, 7), todosOsTimes);
        assertNotNull("Deve retornar um time para 07/01/2024", resultado);
        assertEquals(LocalDate.of(2024, 1, 7), resultado.getData());
    }

    @Test
    public void timeDaData_deveRetornarNullParaDataInexistente() {
        Time resultado = apiService.timeDaData(LocalDate.of(2099, 12, 31), todosOsTimes);
        assertNull("Deve retornar null quando não há time na data informada", resultado);
    }

    @Test
    public void timeDaData_deveRetornarNullParaListaVazia() {
        assertNull(apiService.timeDaData(LocalDate.of(2024, 1, 7), Collections.emptyList()));
    }

    @Test
    public void timeDaData_deveRetornarNullParaDataNull() {
        assertNull(apiService.timeDaData(null, todosOsTimes));
    }

    // =========================================================================
    // integranteMaisUsado
    // =========================================================================

    @Test
    public void integranteMaisUsado_semFiltroDeData_deveRetornarHernanes() {
        // Hernanes: timeSP1 + timeSP2 + timeSP3 + timePalm = 4 aparições (maior)
        Integrante resultado = apiService.integranteMaisUsado(null, null, todosOsTimes);
        assertNotNull(resultado);
        assertEquals("Hernanes deve ser o jogador mais usado no período total",
                hernanes.getId(), resultado.getId());
    }

    @Test
    public void integranteMaisUsado_comFiltroDeData_deveConsiderarApenasOPeriodo() {
        // Fevereiro: apenas timeSantos → Calleri e Kaká com 1 aparição cada (empate)
        Integrante resultado = apiService.integranteMaisUsado(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28), todosOsTimes);
        assertNotNull(resultado);
        assertTrue("Deve retornar Calleri ou Kaká (empate em fev/2024)",
                resultado.getId().equals(calleri.getId()) || resultado.getId().equals(kaka.getId()));
    }

    @Test
    public void integranteMaisUsado_listaVazia_deveRetornarNull() {
        assertNull(apiService.integranteMaisUsado(null, null, Collections.emptyList()));
    }

    // =========================================================================
    // integrantesDoTimeMaisRecorrente
    // =========================================================================

    @Test
    public void integrantesDoTimeMaisRecorrente_deveRetornarJogadoresDoSaoPaulo() {
        // São Paulo aparece 3x → clube mais recorrente
        // Time mais recente do São Paulo: 21/01/2024 → Hernanes e Calleri
        List<String> resultado = apiService.integrantesDoTimeMaisRecorrente(null, null, todosOsTimes);
        assertNotNull(resultado);
        assertFalse("Lista não deve ser vazia", resultado.isEmpty());
        assertTrue("Deve conter Hernanes (integrante do time mais recente do São Paulo)",
                resultado.contains("Hernanes"));
        assertTrue("Deve conter Calleri (integrante do time mais recente do São Paulo)",
                resultado.contains("Calleri"));
    }

    @Test
    public void integrantesDoTimeMaisRecorrente_listaVazia_deveRetornarListaVazia() {
        List<String> resultado = apiService.integrantesDoTimeMaisRecorrente(null, null, Collections.emptyList());
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    public void integrantesDoTimeMaisRecorrente_periodoSemTimes_deveRetornarListaVazia() {
        List<String> resultado = apiService.integrantesDoTimeMaisRecorrente(
                LocalDate.of(2099, 1, 1), LocalDate.of(2099, 12, 31), todosOsTimes);
        assertTrue(resultado.isEmpty());
    }

    // =========================================================================
    // funcaoMaisRecorrente
    // =========================================================================

    @Test
    public void funcaoMaisRecorrente_semFiltro_deveRetornarVolante() {
        // Volante  (Hernanes):   SP1 + SP2 + SP3 + Palm = 4 aparições
        // Meia     (Kaká):       SP1 + SP2 + Santos      = 3 aparições
        // Atacante: LucasMoura(1) + Calleri(2)           = 3 aparições
        // Goleiro  (RogérioCeni): Palm                   = 1 aparição
        String resultado = apiService.funcaoMaisRecorrente(null, null, todosOsTimes);
        assertEquals("Volante deve ser a função mais recorrente no período total", "Volante", resultado);
    }

    @Test
    public void funcaoMaisRecorrente_comFiltroDeData_deveConsiderarApenasOPeriodo() {
        // Janeiro: Volante=4, Meia=2, Atacante=2, Goleiro=1 → Volante
        String resultado = apiService.funcaoMaisRecorrente(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), todosOsTimes);
        assertEquals("Volante", resultado);
    }

    @Test
    public void funcaoMaisRecorrente_listaVazia_deveRetornarNull() {
        assertNull(apiService.funcaoMaisRecorrente(null, null, Collections.emptyList()));
    }

    // =========================================================================
    // clubeMaisRecorrente
    // =========================================================================

    @Test
    public void clubeMaisRecorrente_semFiltro_deveRetornarSaoPaulo() {
        String resultado = apiService.clubeMaisRecorrente(null, null, todosOsTimes);
        assertEquals("São Paulo deve ser o clube mais recorrente (3 aparições)", "São Paulo", resultado);
    }

    @Test
    public void clubeMaisRecorrente_comFiltroDeData_deveRetornarCorreto() {
        // Fevereiro: apenas Santos
        String resultado = apiService.clubeMaisRecorrente(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28), todosOsTimes);
        assertEquals("Santos", resultado);
    }

    @Test
    public void clubeMaisRecorrente_listaVazia_deveRetornarNull() {
        assertNull(apiService.clubeMaisRecorrente(null, null, Collections.emptyList()));
    }

    // =========================================================================
    // contagemDeClubesNoPeriodo
    // =========================================================================

    @Test
    public void contagemDeClubesNoPeriodo_semFiltro_deveRetornarContagensCorretas() {
        Map<String, Long> resultado = apiService.contagemDeClubesNoPeriodo(null, null, todosOsTimes);
        assertEquals("São Paulo deve ter 3 aparições", 3L, (long) resultado.get("São Paulo"));
        assertEquals("Palmeiras deve ter 1 aparição",  1L, (long) resultado.get("Palmeiras"));
        assertEquals("Santos deve ter 1 aparição",     1L, (long) resultado.get("Santos"));
    }

    @Test
    public void contagemDeClubesNoPeriodo_comFiltroDeData_deveConsiderarApenasOPeriodo() {
        // Janeiro: São Paulo=3, Palmeiras=1 (Santos é fevereiro)
        Map<String, Long> resultado = apiService.contagemDeClubesNoPeriodo(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), todosOsTimes);
        assertEquals(3L, (long) resultado.get("São Paulo"));
        assertEquals(1L, (long) resultado.get("Palmeiras"));
        assertFalse("Santos não deve aparecer em janeiro", resultado.containsKey("Santos"));
    }

    @Test
    public void contagemDeClubesNoPeriodo_listaVazia_deveRetornarMapaVazio() {
        assertTrue(apiService.contagemDeClubesNoPeriodo(null, null, Collections.emptyList()).isEmpty());
    }

    // =========================================================================
    // contagemPorFuncao
    // =========================================================================

    @Test
    public void contagemPorFuncao_semFiltro_deveRetornarContagensCorretas() {
        Map<String, Long> resultado = apiService.contagemPorFuncao(null, null, todosOsTimes);
        // Volante  (Hernanes):    SP1 + SP2 + SP3 + Palm             = 4
        assertEquals("Volante deve ter 4 aparições",  4L, (long) resultado.get("Volante"));
        // Meia     (Kaká):        SP1 + SP2 + Santos                 = 3
        assertEquals("Meia deve ter 3 aparições",     3L, (long) resultado.get("Meia"));
        // Atacante: LucasMoura(SP1=1) + Calleri(SP3 + Santos = 2)   = 3
        assertEquals("Atacante deve ter 3 aparições", 3L, (long) resultado.get("Atacante"));
        // Goleiro  (RogérioCeni): Palm                               = 1
        assertEquals("Goleiro deve ter 1 aparição",   1L, (long) resultado.get("Goleiro"));
    }

    @Test
    public void contagemPorFuncao_comFiltroDeData_deveConsiderarApenasOPeriodo() {
        // Fevereiro: timeSantos → Calleri (Atacante) + Kaká (Meia) = 1 cada
        Map<String, Long> resultado = apiService.contagemPorFuncao(
                LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 28), todosOsTimes);
        assertEquals(1L, (long) resultado.get("Atacante"));
        assertEquals(1L, (long) resultado.get("Meia"));
        assertFalse("Volante não deve aparecer em fevereiro", resultado.containsKey("Volante"));
        assertFalse("Goleiro não deve aparecer em fevereiro", resultado.containsKey("Goleiro"));
    }

    @Test
    public void contagemPorFuncao_listaVazia_deveRetornarMapaVazio() {
        assertTrue(apiService.contagemPorFuncao(null, null, Collections.emptyList()).isEmpty());
    }

    // =========================================================================
    // Helpers de construção de objetos
    // =========================================================================

    /**
     * Cria um {@link Integrante} com id, nome e função definidos.
     */
    private Integrante criarIntegrante(Long id, String nome, String funcao) {
        return Integrante.builder()
                .id(id)
                .nome(nome)
                .funcao(funcao)
                .build();
    }

    /**
     * Cria um {@link Time} com clube, data e jogadores, montando as composições automaticamente.
     */
    private Time criarTime(Long id, String nomeClube, LocalDate data, Integrante... integrantes) {
        Time time = Time.builder()
                .id(id)
                .nomeClube(nomeClube)
                .data(data)
                .build();

        for (Integrante integrante : integrantes) {
            ComposicaoTime composicao = ComposicaoTime.builder()
                    .id(null)
                    .time(time)
                    .integrante(integrante)
                    .build();
            time.getComposicaoTimes().add(composicao);
        }

        return time;
    }
}
