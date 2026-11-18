package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.LancheDTO;
import br.com.inteligencia.artificial.lanchonete.service.LancheService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

	private final LancheService lancheService;

	public ProdutoController(LancheService lancheService) {
		this.lancheService = lancheService;
	}

	@GetMapping
	public ResponseEntity<List<LancheDTO>> listar() {
		return ResponseEntity.ok(lancheService.listarTodos());
	}
}
