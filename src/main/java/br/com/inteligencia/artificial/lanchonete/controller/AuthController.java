package br.com.inteligencia.artificial.lanchonete.controller;

import br.com.inteligencia.artificial.lanchonete.dto.AlterarSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.LoginRequest;
import br.com.inteligencia.artificial.lanchonete.dto.RecuperarSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.RedefinirSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.UsuarioDTO;
import br.com.inteligencia.artificial.lanchonete.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/registro")
	public ResponseEntity<UsuarioDTO> registrar(@RequestBody UsuarioDTO dto) {
		return ResponseEntity.status(201).body(authService.registrar(dto));
	}

	@PostMapping("/login")
	public ResponseEntity<UsuarioDTO> login(
			@RequestBody LoginRequest request,
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse
	) {
		return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
		authService.logout(request, response);
		return ResponseEntity.ok(Map.of("message", "Logout realizado"));
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioDTO> me() {
		return ResponseEntity.ok(authService.me());
	}

	@PostMapping("/alterar-senha")
	public ResponseEntity<Map<String, String>> alterarSenha(@RequestBody AlterarSenhaRequest request) {
		authService.alterarSenha(request);
		return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
	}

	@PostMapping("/recuperar-senha")
	public ResponseEntity<Map<String, String>> recuperarSenha(@RequestBody RecuperarSenhaRequest request) {
		return ResponseEntity.ok(authService.recuperarSenha(request));
	}

	@PostMapping("/redefinir-senha")
	public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody RedefinirSenhaRequest request) {
		authService.redefinirSenha(request);
		return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso"));
	}
}
