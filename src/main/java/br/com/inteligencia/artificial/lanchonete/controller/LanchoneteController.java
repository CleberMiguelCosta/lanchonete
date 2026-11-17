package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.LanchoneteDTO;
import br.com.inteligencia.artificial.lanchonete.service.LanchoneteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lanchonetes")
public class LanchoneteController {

	private final LanchoneteService lanchoneteService;

	public LanchoneteController(LanchoneteService lanchoneteService) {
		this.lanchoneteService = lanchoneteService;
	}

	@GetMapping
	public ResponseEntity<List<LanchoneteDTO>> listarTodas() {
		return ResponseEntity.ok(lanchoneteService.listarTodas());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LanchoneteDTO> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(lanchoneteService.buscarPorId(id));
	}

	@PostMapping
	public ResponseEntity<LanchoneteDTO> criar(@RequestBody LanchoneteDTO dto) {
		LanchoneteDTO criada = lanchoneteService.criar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(criada);
	}

	@PutMapping("/{id}")
	public ResponseEntity<LanchoneteDTO> atualizar(@PathVariable Long id, @RequestBody LanchoneteDTO dto) {
		return ResponseEntity.ok(lanchoneteService.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		lanchoneteService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
