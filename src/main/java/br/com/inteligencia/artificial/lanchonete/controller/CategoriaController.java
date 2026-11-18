package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.CategoriaDTO;
import br.com.inteligencia.artificial.lanchonete.service.CategoriaService;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

	private final CategoriaService categoriaService;

	public CategoriaController(CategoriaService categoriaService) {
		this.categoriaService = categoriaService;
	}

	@GetMapping
	public ResponseEntity<List<CategoriaDTO>> listar() {
		return ResponseEntity.ok(categoriaService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoriaDTO> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(categoriaService.buscar(id));
	}

	@PostMapping
	public ResponseEntity<CategoriaDTO> criar(@RequestBody CategoriaDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(dto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
		return ResponseEntity.ok(categoriaService.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		categoriaService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
