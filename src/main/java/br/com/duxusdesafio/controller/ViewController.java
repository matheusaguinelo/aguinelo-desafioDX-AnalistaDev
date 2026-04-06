package br.com.duxusdesafio.controller;

import br.com.duxusdesafio.repository.IntegranteRepository;
import br.com.duxusdesafio.repository.TimeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller MVC responsável por servir as páginas Thymeleaf do frontend.
 */
@Controller
public class ViewController {

    private final IntegranteRepository integranteRepository;
    private final TimeRepository timeRepository;

    public ViewController(IntegranteRepository integranteRepository, TimeRepository timeRepository) {
        this.integranteRepository = integranteRepository;
        this.timeRepository = timeRepository;
    }

    /** Redireciona a raiz para a página de integrantes. */
    @GetMapping("/")
    public String home() {
        return "redirect:/integrantes";
    }

    /** Página de cadastro e listagem de integrantes. */
    @GetMapping("/integrantes")
    public String integrantes(Model model) {
        model.addAttribute("integrantes", integranteRepository.findAll());
        return "integrantes";
    }

    /** Página de montagem e listagem de times. */
    @GetMapping("/times")
    public String times(Model model) {
        model.addAttribute("times", timeRepository.findAll());
        model.addAttribute("integrantes", integranteRepository.findAll());
        return "times";
    }

    /** Página de consultas analíticas. */
    @GetMapping("/consultas")
    public String consultas() {
        return "consultas";
    }
}
