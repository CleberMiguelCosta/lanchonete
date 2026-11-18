package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.PedidoDTO;
import br.com.inteligencia.artificial.lanchonete.model.PedidoStatus;
import br.com.inteligencia.artificial.lanchonete.service.PedidoService;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping
	public ResponseEntity<List<PedidoDTO>> listar() {
		return ResponseEntity.ok(pedidoService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PedidoDTO> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(pedidoService.buscar(id));
	}

	@PostMapping
	public ResponseEntity<PedidoDTO> criar(@RequestBody PedidoDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.criar(dto));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<PedidoDTO> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
		PedidoStatus status = PedidoStatus.valueOf(body.get("status"));
		return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		pedidoService.deletar(id);
		return ResponseEntity.noContent().build();
	}
}
