package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.LancheDTO;
import br.com.inteligencia.artificial.lanchonete.service.LancheService;
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
@RequestMapping("/api/lanchonetes/{lanchoneteId}/lanches")
public class LancheController {

	private final LancheService lancheService;

	public LancheController(LancheService lancheService) {
		this.lancheService = lancheService;
	}

	@GetMapping
	public ResponseEntity<List<LancheDTO>> listar(@PathVariable Long lanchoneteId) {
		return ResponseEntity.ok(lancheService.listarPorLanchonete(lanchoneteId));
	}

	@GetMapping("/{lancheId}")
	public ResponseEntity<LancheDTO> buscar(@PathVariable Long lanchoneteId, @PathVariable Long lancheId) {
		return ResponseEntity.ok(lancheService.buscarPorId(lanchoneteId, lancheId));
	}

	@PostMapping
	public ResponseEntity<LancheDTO> criar(@PathVariable Long lanchoneteId, @RequestBody LancheDTO dto) {
		LancheDTO criado = lancheService.criar(lanchoneteId, dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(criado);
	}

	@PutMapping("/{lancheId}")
	public ResponseEntity<LancheDTO> atualizar(
			@PathVariable Long lanchoneteId,
			@PathVariable Long lancheId,
			@RequestBody LancheDTO dto
	) {
		return ResponseEntity.ok(lancheService.atualizar(lanchoneteId, lancheId, dto));
	}

	@DeleteMapping("/{lancheId}")
	public ResponseEntity<Void> deletar(@PathVariable Long lanchoneteId, @PathVariable Long lancheId) {
		lancheService.deletar(lanchoneteId, lancheId);
		return ResponseEntity.noContent().build();
	}
}
