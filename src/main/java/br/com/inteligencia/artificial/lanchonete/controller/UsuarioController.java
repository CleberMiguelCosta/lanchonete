package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.UsuarioDTO;
import br.com.inteligencia.artificial.lanchonete.model.AcessoLog;
import br.com.inteligencia.artificial.lanchonete.model.AtividadeLog;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import br.com.inteligencia.artificial.lanchonete.service.AuditoriaService;
import br.com.inteligencia.artificial.lanchonete.service.UsuarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;
	private final AuditoriaService auditoriaService;

	public UsuarioController(UsuarioService usuarioService, AuditoriaService auditoriaService) {
		this.usuarioService = usuarioService;
		this.auditoriaService = auditoriaService;
	}

	@GetMapping
	public ResponseEntity<List<UsuarioDTO>> listar(@RequestParam(required = false) String q) {
		return ResponseEntity.ok(usuarioService.listar(q));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
		return ResponseEntity.ok(usuarioService.buscar(id));
	}

	@PostMapping
	public ResponseEntity<UsuarioDTO> criar(@RequestBody UsuarioDTO dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(dto));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UsuarioDTO> atualizar(@PathVariable Long id, @RequestBody UsuarioDTO dto) {
		return ResponseEntity.ok(usuarioService.atualizar(id, dto));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<UsuarioDTO> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
		StatusUsuario status = StatusUsuario.valueOf(body.get("status"));
		return ResponseEntity.ok(usuarioService.alterarStatus(id, status));
	}

	@PostMapping("/{id}/redefinir-senha")
	public ResponseEntity<UsuarioDTO> redefinirSenha(
			@PathVariable Long id,
			@RequestBody(required = false) Map<String, Object> body
	) {
		boolean enviarEmail = body == null || body.get("enviarEmail") == null || Boolean.TRUE.equals(body.get("enviarEmail"));
		return ResponseEntity.ok(usuarioService.redefinirSenha(id, enviarEmail));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		usuarioService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/auditoria/acessos")
	public ResponseEntity<List<AcessoLog>> acessos() {
		return ResponseEntity.ok(auditoriaService.ultimosAcessos());
	}

	@GetMapping("/auditoria/atividades")
	public ResponseEntity<List<AtividadeLog>> atividades() {
		return ResponseEntity.ok(auditoriaService.ultimasAtividades());
	}

	@GetMapping("/{id}/acessos")
	public ResponseEntity<List<AcessoLog>> acessosUsuario(@PathVariable Long id) {
		return ResponseEntity.ok(auditoriaService.acessosDoUsuario(id));
	}

	@GetMapping("/{id}/atividades")
	public ResponseEntity<List<AtividadeLog>> atividadesUsuario(@PathVariable Long id) {
		return ResponseEntity.ok(auditoriaService.atividadesDoUsuario(id));
	}
}
