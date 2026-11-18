package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.AlterarSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.LoginRequest;
import br.com.inteligencia.artificial.lanchonete.dto.RecuperarSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.RedefinirSenhaRequest;
import br.com.inteligencia.artificial.lanchonete.dto.UsuarioDTO;
import br.com.inteligencia.artificial.lanchonete.model.PasswordResetToken;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import br.com.inteligencia.artificial.lanchonete.model.Usuario;
import br.com.inteligencia.artificial.lanchonete.repository.PasswordResetTokenRepository;
import br.com.inteligencia.artificial.lanchonete.repository.UsuarioRepository;
import br.com.inteligencia.artificial.lanchonete.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;
	private final UsuarioRepository usuarioRepository;
	private final PasswordResetTokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioService usuarioService;
	private final AuditoriaService auditoriaService;
	private final EmailService emailService;

	public AuthService(
			AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			UsuarioRepository usuarioRepository,
			PasswordResetTokenRepository tokenRepository,
			PasswordEncoder passwordEncoder,
			UsuarioService usuarioService,
			AuditoriaService auditoriaService,
			EmailService emailService
	) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.usuarioRepository = usuarioRepository;
		this.tokenRepository = tokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.usuarioService = usuarioService;
		this.auditoriaService = auditoriaService;
		this.emailService = emailService;
	}

	@Transactional
	public UsuarioDTO registrar(UsuarioDTO dto) {
		return usuarioService.registrarContaPublica(dto);
	}

	@Transactional
	public UsuarioDTO login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		String username = request.getUsername() == null ? "" : request.getUsername().trim();
		String ip = httpRequest.getRemoteAddr();
		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(username, request.getSenha())
			);
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);
			securityContextRepository.saveContext(context, httpRequest, httpResponse);

			UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
			Usuario usuario = usuarioRepository.findById(principal.getId()).orElseThrow();
			usuario.setUltimoAcesso(LocalDateTime.now());
			usuarioRepository.save(usuario);

			auditoriaService.registrarAcesso(usuario.getId(), usuario.getUsername(), true, ip, "Login realizado");
			return usuarioService.paraDTO(usuario);
		} catch (BadCredentialsException ex) {
			auditoriaService.registrarAcesso(null, username, false, ip, "Credenciais inválidas");
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
		} catch (DisabledException | LockedException ex) {
			auditoriaService.registrarAcesso(null, username, false, ip, "Usuário bloqueado/inativo");
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo ou bloqueado");
		}
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
			auditoriaService.registrarAcesso(principal.getId(), principal.getUsername(), true, request.getRemoteAddr(), "Logout");
		}
		SecurityContextHolder.clearContext();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	public UsuarioDTO me() {
		UsuarioPrincipal principal = usuarioAtual();
		Usuario usuario = usuarioRepository.findById(principal.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida"));
		return usuarioService.paraDTO(usuario);
	}

	@Transactional
	public void alterarSenha(AlterarSenhaRequest request) {
		UsuarioPrincipal principal = usuarioAtual();
		Usuario usuario = usuarioRepository.findById(principal.getId()).orElseThrow();
		if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
		}
		validarNovaSenha(request.getNovaSenha(), request.getConfirmarSenha());
		usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
		usuario.setDeveAlterarSenha(false);
		usuarioRepository.save(usuario);
		auditoriaService.registrarAtividade(usuario.getId(), usuario.getUsername(), "ALTERAR_SENHA", "Senha alterada pelo próprio usuário");
	}

	@Transactional
	public Map<String, String> recuperarSenha(RecuperarSenhaRequest request) {
		String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
		if (usuario == null || usuario.getStatus() != StatusUsuario.ATIVO) {
			return Map.of("message", "Se o e-mail existir, enviaremos as instruções de recuperação.");
		}

		PasswordResetToken token = new PasswordResetToken();
		token.setToken(UUID.randomUUID().toString().replace("-", ""));
		token.setUsuarioId(usuario.getId());
		token.setExpiraEm(LocalDateTime.now().plusHours(2));
		tokenRepository.save(token);

		String corpo = """
				Olá %s,

				Use o token abaixo para redefinir sua senha (válido por 2 horas):

				Token: %s

				Acesse a tela de redefinição e informe este token com a nova senha.
				""".formatted(usuario.getNomeCompleto(), token.getToken());
		emailService.enviar(usuario.getEmail(), "Recuperação de senha - Lanchonete OS", corpo);
		auditoriaService.registrarAtividade(usuario.getId(), usuario.getUsername(), "RECUPERAR_SENHA", "Solicitação de recuperação");

		return Map.of(
				"message", "Se o e-mail existir, enviaremos as instruções de recuperação.",
				"tokenDev", token.getToken()
		);
	}

	@Transactional
	public void redefinirSenha(RedefinirSenhaRequest request) {
		PasswordResetToken token = tokenRepository.findByToken(request.getToken())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido"));
		if (token.isUsado() || token.getExpiraEm().isBefore(LocalDateTime.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado ou já utilizado");
		}
		validarNovaSenha(request.getNovaSenha(), request.getConfirmarSenha());
		Usuario usuario = usuarioRepository.findById(token.getUsuarioId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));
		usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
		usuario.setDeveAlterarSenha(false);
		usuarioRepository.save(usuario);
		token.setUsado(true);
		tokenRepository.save(token);
		auditoriaService.registrarAtividade(usuario.getId(), usuario.getUsername(), "REDEFINIR_SENHA", "Senha redefinida via token");
	}

	private void validarNovaSenha(String nova, String confirmar) {
		if (nova == null || nova.length() < 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nova senha deve ter ao menos 6 caracteres");
		}
		if (confirmar == null || !nova.equals(confirmar)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmação de senha não confere");
		}
	}

	private UsuarioPrincipal usuarioAtual() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof UsuarioPrincipal principal)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
		}
		return principal;
	}
}
