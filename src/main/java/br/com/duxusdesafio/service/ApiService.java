package br.com.duxusdesafio.service;

import br.com.duxusdesafio.exception.InvalidDateRangeException;
import br.com.duxusdesafio.model.ComposicaoTime;
import br.com.duxusdesafio.model.Integrante;
import br.com.duxusdesafio.model.Time;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service com as regras de negócio para processamento dos dados de times.
 *
 * <p><b>Regra importante:</b> todos os métodos recebem a lista completa de Times
 * já carregada do banco e processam os dados exclusivamente em Java (sem COUNT/GROUP BY
 * no SQL), conforme exigido pelo desafio.</p>
 *
 * <p><b>Validação de período:</b> quando ambas as datas são fornecidas,
 * {@code dataInicial} não pode ser posterior a {@code dataFinal}.</p>
 */
@Slf4j
@Service
public class ApiService {

    // =========================================================================
    // Métodos auxiliares
    // =========================================================================

    /**
     * Valida que o intervalo de datas é coerente.
     * Lança {@link InvalidDateRangeException} se {@code dataInicial} for
     * posterior a {@code dataFinal}.
     *
     * @param dataInicial início do período (pode ser null)
     * @param dataFinal   fim do período (pode ser null)
     */
    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new InvalidDateRangeException(
                    "dataInicial (" + dataInicial + ") não pode ser posterior a dataFinal (" + dataFinal + ").");
        }
    }

    /**
     * Filtra a lista de times pelo intervalo de datas fornecido.
     * Se {@code dataInicial} for {@code null}, não há limite inferior.
     * Se {@code dataFinal} for {@code null}, não há limite superior.
     *
     * @param dataInicial data de início do período (inclusive), pode ser null
     * @param dataFinal   data de fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return lista filtrada pelo período
     */
    private List<Time> filtrarPorPeriodo(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        return todosOsTimes.stream()
                .filter(t -> dataInicial == null || !t.getData().isBefore(dataInicial))
                .filter(t -> dataFinal == null || !t.getData().isAfter(dataFinal))
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Métodos principais
    // =========================================================================

    /**
     * Retorna o {@link Time} cuja data coincide exatamente com {@code data}.
     * Se não houver time naquela data, retorna {@code null}.
     *
     * @param data         data exata a pesquisar
     * @param todosOsTimes lista completa de times
     * @return o Time daquela data ou {@code null}
     */
    public Time timeDaData(LocalDate data, List<Time> todosOsTimes) {
        if (data == null || todosOsTimes == null) return null;
        log.debug("Consultando time para data={}", data);

        return todosOsTimes.stream()
                .filter(t -> data.equals(t.getData()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna o {@link Integrante} que aparece na maior quantidade de times
     * dentro do período informado.
     *
     * <p>A contagem é feita percorrendo todas as {@link ComposicaoTime} dos times
     * filtrados e agrupando por integrante (usando o id como chave).</p>
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return o Integrante mais usado ou {@code null} se não houver dados
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public Integrante integranteMaisUsado(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return null;
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando integrante mais usado no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        // Mapeia id do integrante → contagem de aparições
        Map<Long, Long> contagemPorId = timesFiltrados.stream()
                .flatMap(t -> t.getComposicaoTimes().stream())
                .map(ComposicaoTime::getIntegrante)
                .collect(Collectors.groupingBy(Integrante::getId, Collectors.counting()));

        Optional<Map.Entry<Long, Long>> entradaMax = contagemPorId.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (!entradaMax.isPresent()) return null;

        Long idMaisUsado = entradaMax.get().getKey();

        return timesFiltrados.stream()
                .flatMap(t -> t.getComposicaoTimes().stream())
                .map(ComposicaoTime::getIntegrante)
                .filter(i -> idMaisUsado.equals(i.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retorna os nomes dos integrantes do time (clube) mais recorrente no período.
     *
     * <p>O "time mais recorrente" é o clube que possui o maior número de entradas
     * (registros de {@link Time}) no período. Em caso de empate, prevalece o clube
     * com o registro mais recente. Os integrantes retornados são os do time
     * mais recente desse clube.</p>
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return lista de nomes dos integrantes ou lista vazia
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public List<String> integrantesDoTimeMaisRecorrente(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return Collections.emptyList();
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando integrantes do time mais recorrente no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        // Conta quantas vezes cada clube aparece no período
        Map<String, Long> contagemPorClube = timesFiltrados.stream()
                .collect(Collectors.groupingBy(Time::getNomeClube, Collectors.counting()));

        Optional<String> clubeMax = contagemPorClube.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);

        if (!clubeMax.isPresent()) return Collections.emptyList();

        String clube = clubeMax.get();

        // Pega o time mais recente desse clube e retorna os nomes dos integrantes
        return timesFiltrados.stream()
                .filter(t -> clube.equals(t.getNomeClube()))
                .max(Comparator.comparing(Time::getData))
                .map(t -> t.getComposicaoTimes().stream()
                        .map(ct -> ct.getIntegrante().getNome())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    /**
     * Retorna a função mais recorrente entre todos os integrantes presentes
     * nos times do período.
     *
     * <p>Cada aparição de um integrante em um time conta como +1 para sua função,
     * mesmo que o mesmo integrante apareça em múltiplos times.</p>
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return nome da função mais recorrente ou {@code null}
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public String funcaoMaisRecorrente(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return null;
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando função mais recorrente no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        return timesFiltrados.stream()
                .flatMap(t -> t.getComposicaoTimes().stream())
                .map(ct -> ct.getIntegrante().getFuncao())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Retorna o nome do clube que aparece com maior frequência no período.
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return nome do clube mais recorrente ou {@code null}
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public String clubeMaisRecorrente(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return null;
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando clube mais recorrente no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        return timesFiltrados.stream()
                .collect(Collectors.groupingBy(Time::getNomeClube, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * Retorna um mapa com a quantidade de aparições de cada clube no período.
     * Exemplo: {@code {"Falcons": 5, "FURIA": 2, "DarkZero Esports": 3}}.
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return mapa clube → quantidade de aparições
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public Map<String, Long> contagemDeClubesNoPeriodo(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return Collections.emptyMap();
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando contagem de clubes no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        return timesFiltrados.stream()
                .collect(Collectors.groupingBy(Time::getNomeClube, Collectors.counting()));
    }

    /**
     * Retorna um mapa com a quantidade de vezes que cada função aparece nos times do período.
     * Contabiliza cada vínculo {@link ComposicaoTime} individualmente (uma função pode ser
     * contada múltiplas vezes se o mesmo integrante participar de vários times).
     *
     * @param dataInicial  início do período (inclusive), pode ser null
     * @param dataFinal    fim do período (inclusive), pode ser null
     * @param todosOsTimes lista completa de times
     * @return mapa função → quantidade de aparições
     * @throws InvalidDateRangeException se dataInicial for posterior a dataFinal
     */
    public Map<String, Long> contagemPorFuncao(LocalDate dataInicial, LocalDate dataFinal, List<Time> todosOsTimes) {
        if (todosOsTimes == null) return Collections.emptyMap();
        validarPeriodo(dataInicial, dataFinal);
        log.debug("Calculando contagem por função no período [{}, {}]", dataInicial, dataFinal);

        List<Time> timesFiltrados = filtrarPorPeriodo(dataInicial, dataFinal, todosOsTimes);

        return timesFiltrados.stream()
                .flatMap(t -> t.getComposicaoTimes().stream())
                .map(ct -> ct.getIntegrante().getFuncao())
                .collect(Collectors.groupingBy(f -> f, Collectors.counting()));
    }
}
