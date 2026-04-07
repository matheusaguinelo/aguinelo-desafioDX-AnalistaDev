package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.service.IntegranteService;
import br.com.duxusdesafio.service.TimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller MVC responsável por servir as páginas Thymeleaf do frontend.
 *
 * <p>Usa {@link IntegranteService} e {@link TimeService} para popular os modelos
 * das views, garantindo que as coleções sejam carregadas dentro de um contexto
 * transacional (evitando LazyInitializationException).</p>
 */
@Controller
public class ViewController {

    private final IntegranteService integranteService;
    private final TimeService timeService;

    public ViewController(IntegranteService integranteService, TimeService timeService) {
        this.integranteService = integranteService;
        this.timeService = timeService;
    }

    /** Redireciona a raiz para a página de integrantes. */
    @GetMapping("/")
    public String home() {
        return "redirect:/integrantes";
    }

    /** Página de cadastro e listagem de integrantes. */
    @GetMapping("/integrantes")
    public String integrantes(Model model) {
        model.addAttribute("integrantes", integranteService.listarTodos());
        return "integrantes";
    }

    /** Página de montagem e listagem de times. */
    @GetMapping("/times")
    public String times(Model model) {
        model.addAttribute("times", timeService.listarTodosParaView());
        model.addAttribute("integrantes", integranteService.listarTodos());
        return "times";
    }

    /** Página de consultas analíticas. */
    @GetMapping("/consultas")
    public String consultas() {
        return "consultas";
    }
}
