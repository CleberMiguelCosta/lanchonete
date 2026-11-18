package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.UsuarioDTO;
import br.com.inteligencia.artificial.lanchonete.model.ModuloSistema;
import br.com.inteligencia.artificial.lanchonete.model.PerfilUsuario;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import br.com.inteligencia.artificial.lanchonete.model.Usuario;
import br.com.inteligencia.artificial.lanchonete.repository.UsuarioRepository;
import br.com.inteligencia.artificial.lanchonete.security.UsuarioPrincipal;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	private static final String SENHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#";
	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;
	private final AuditoriaService auditoriaService;
	private final SecureRandom random = new SecureRandom();

	public UsuarioService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			EmailService emailService,
			AuditoriaService auditoriaService
	) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailService = emailService;
		this.auditoriaService = auditoriaService;
	}

	public List<UsuarioDTO> listar(String busca) {
		List<Usuario> lista = (busca == null || busca.isBlank())
				? usuarioRepository.findAllByOrderByNomeCompletoAsc()
				: usuarioRepository.buscar(busca.trim());
		return lista.stream().map(this::paraDTO).toList();
	}

	public UsuarioDTO buscar(Long id) {
		return paraDTO(buscarEntidade(id));
	}

	@Transactional
	public UsuarioDTO criar(UsuarioDTO dto) {
		validarCadastro(dto, null);
		Usuario usuario = new Usuario();
		aplicarDados(usuario, dto, true);

		String senhaPlain = resolverSenha(dto);
		usuario.setSenhaHash(passwordEncoder.encode(senhaPlain));
		usuario.setDeveAlterarSenha(true);
		usuario.setPermissoes(resolverPermissoes(dto));

		Usuario salvo = usuarioRepository.save(usuario);
		registrarAtividade("CRIAR_USUARIO", "Usuário criado: " + salvo.getUsername());

		if (dto.isEnviarEmailAcesso()) {
			enviarCredenciais(salvo, senhaPlain);
		}

		UsuarioDTO resposta = paraDTO(salvo);
		resposta.setSenhaProvisoria(senhaPlain);
		return resposta;
	}

	@Transactional
	public UsuarioDTO registrarContaPublica(UsuarioDTO dto) {
		if (dto.getSenha() == null || dto.getSenha().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha é obrigatória");
		}
		if (dto.getConfirmarSenha() == null || !dto.getSenha().equals(dto.getConfirmarSenha())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmação de senha não confere");
		}
		if (dto.getSenha().length() < 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha deve ter ao menos 6 caracteres");
		}

		dto.setPerfil(PerfilUsuario.USUARIO);
		dto.setStatus(StatusUsuario.ATIVO);
		dto.setGerarSenhaProvisoria(false);
		dto.setEnviarEmailAcesso(false);
		dto.setPermissoes(EnumSet.of(
				ModuloSistema.DASHBOARD,
				ModuloSistema.PEDIDOS,
				ModuloSistema.CARDAPIO
		));

		validarCadastro(dto, null);
		Usuario usuario = new Usuario();
		aplicarDados(usuario, dto, true);
		usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));
		usuario.setDeveAlterarSenha(false);
		usuario.setPermissoes(dto.getPermissoes());

		Usuario salvo = usuarioRepository.save(usuario);
		auditoriaService.registrarAtividade(salvo.getId(), salvo.getUsername(), "REGISTRO_PUBLICO", "Conta criada pelo próprio usuário");
		return paraDTO(salvo);
	}

	@Transactional
	public UsuarioDTO atualizar(Long id, UsuarioDTO dto) {
		Usuario usuario = buscarEntidade(id);
		validarCadastro(dto, id);
		aplicarDados(usuario, dto, false);
		usuario.setPermissoes(resolverPermissoes(dto));
		Usuario salvo = usuarioRepository.save(usuario);
		registrarAtividade("ATUALIZAR_USUARIO", "Usuário atualizado: " + salvo.getUsername());
		return paraDTO(salvo);
	}

	@Transactional
	public UsuarioDTO alterarStatus(Long id, StatusUsuario status) {
		Usuario usuario = buscarEntidade(id);
		usuario.setStatus(status);
		Usuario salvo = usuarioRepository.save(usuario);
		registrarAtividade("STATUS_USUARIO", "Status de " + salvo.getUsername() + " -> " + status);
		return paraDTO(salvo);
	}

	@Transactional
	public UsuarioDTO redefinirSenha(Long id, boolean enviarEmail) {
		Usuario usuario = buscarEntidade(id);
		String senhaPlain = gerarSenhaProvisoria();
		usuario.setSenhaHash(passwordEncoder.encode(senhaPlain));
		usuario.setDeveAlterarSenha(true);
		Usuario salvo = usuarioRepository.save(usuario);
		registrarAtividade("RESET_SENHA", "Senha redefinida para " + salvo.getUsername());
		if (enviarEmail) {
			enviarCredenciais(salvo, senhaPlain);
		}
		UsuarioDTO dto = paraDTO(salvo);
		dto.setSenhaProvisoria(senhaPlain);
		return dto;
	}

	@Transactional
	public void excluir(Long id) {
		Usuario usuario = buscarEntidade(id);
		if ("admin".equalsIgnoreCase(usuario.getUsername())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido excluir o administrador padrão");
		}
		usuarioRepository.delete(usuario);
		registrarAtividade("EXCLUIR_USUARIO", "Usuário excluído: " + usuario.getUsername());
	}

	public Usuario buscarEntidade(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	public UsuarioDTO paraDTO(Usuario usuario) {
		UsuarioDTO dto = new UsuarioDTO();
		dto.setId(usuario.getId());
		dto.setNomeCompleto(usuario.getNomeCompleto());
		dto.setCpf(usuario.getCpf());
		dto.setEmail(usuario.getEmail());
		dto.setTelefone(usuario.getTelefone());
		dto.setUsername(usuario.getUsername());
		dto.setPerfil(usuario.getPerfil());
		dto.setStatus(usuario.getStatus());
		dto.setDeveAlterarSenha(usuario.isDeveAlterarSenha());
		dto.setCriadoEm(usuario.getCriadoEm());
		dto.setUltimoAcesso(usuario.getUltimoAcesso());
		dto.setPermissoes(usuario.getPermissoes());
		return dto;
	}

	private void aplicarDados(Usuario usuario, UsuarioDTO dto, boolean criacao) {
		usuario.setNomeCompleto(dto.getNomeCompleto().trim());
		usuario.setCpf(normalizarCpf(dto.getCpf()));
		usuario.setEmail(dto.getEmail().trim().toLowerCase());
		usuario.setTelefone(blankToNull(dto.getTelefone()));
		usuario.setUsername(dto.getUsername().trim().toLowerCase());
		usuario.setPerfil(dto.getPerfil());
		usuario.setStatus(dto.getStatus() == null ? StatusUsuario.ATIVO : dto.getStatus());
		if (criacao) {
			usuario.setDeveAlterarSenha(true);
		}
	}

	private Set<ModuloSistema> resolverPermissoes(UsuarioDTO dto) {
		if (dto.getPerfil() == PerfilUsuario.ADMINISTRADOR) {
			return EnumSet.allOf(ModuloSistema.class);
		}
		if (dto.getPermissoes() == null || dto.getPermissoes().isEmpty()) {
			if (dto.getPerfil() == PerfilUsuario.GESTOR) {
				return EnumSet.of(
						ModuloSistema.DASHBOARD,
						ModuloSistema.PEDIDOS,
						ModuloSistema.CARDAPIO,
						ModuloSistema.PRODUTOS,
						ModuloSistema.CATEGORIAS,
						ModuloSistema.CLIENTES,
						ModuloSistema.RELATORIOS,
						ModuloSistema.LANCHONETES
				);
			}
			return EnumSet.of(ModuloSistema.DASHBOARD, ModuloSistema.PEDIDOS, ModuloSistema.CARDAPIO);
		}
		return dto.getPermissoes().stream().collect(Collectors.toCollection(() -> EnumSet.noneOf(ModuloSistema.class)));
	}

	private String resolverSenha(UsuarioDTO dto) {
		if (dto.isGerarSenhaProvisoria() || dto.getSenha() == null || dto.getSenha().isBlank()) {
			return gerarSenhaProvisoria();
		}
		if (dto.getConfirmarSenha() == null || !dto.getSenha().equals(dto.getConfirmarSenha())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmação de senha não confere");
		}
		if (dto.getSenha().length() < 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha deve ter ao menos 6 caracteres");
		}
		return dto.getSenha();
	}

	private void validarCadastro(UsuarioDTO dto, Long idAtual) {
		if (dto.getNomeCompleto() == null || dto.getNomeCompleto().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome completo é obrigatório");
		}
		if (dto.getCpf() == null || !validarCpf(dto.getCpf())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido");
		}
		if (dto.getEmail() == null || dto.getEmail().isBlank() || !dto.getEmail().contains("@")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail inválido");
		}
		if (dto.getUsername() == null || dto.getUsername().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de usuário é obrigatório");
		}
		if (dto.getPerfil() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de acesso é obrigatório");
		}

		String cpf = normalizarCpf(dto.getCpf());
		String username = dto.getUsername().trim().toLowerCase();
		String email = dto.getEmail().trim().toLowerCase();

		usuarioRepository.findByUsernameIgnoreCase(username).ifPresent(u -> {
			if (idAtual == null || !u.getId().equals(idAtual)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Username já utilizado");
			}
		});
		usuarioRepository.findByEmailIgnoreCase(email).ifPresent(u -> {
			if (idAtual == null || !u.getId().equals(idAtual)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já utilizado");
			}
		});
		if (idAtual == null && usuarioRepository.existsByCpf(cpf)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
		}
		if (idAtual != null) {
			Usuario atual = buscarEntidade(idAtual);
			if (!atual.getCpf().equals(cpf) && usuarioRepository.existsByCpf(cpf)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF já cadastrado");
			}
		}
	}

	private void enviarCredenciais(Usuario usuario, String senhaPlain) {
		String corpo = """
				Olá %s,

				Seu acesso ao Lanchonete OS foi criado.

				Login: %s
				Senha provisória: %s

				No primeiro acesso, altere sua senha.

				Equipe Lanchonete OS
				""".formatted(usuario.getNomeCompleto(), usuario.getUsername(), senhaPlain);
		emailService.enviar(usuario.getEmail(), "Acesso ao Lanchonete OS", corpo);
	}

	public String gerarSenhaProvisoria() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append(SENHA_CHARS.charAt(random.nextInt(SENHA_CHARS.length())));
		}
		return sb.toString();
	}

	private boolean validarCpf(String cpfRaw) {
		String cpf = normalizarCpf(cpfRaw);
		if (cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
			return false;
		}
		try {
			int dig1 = calcularDigito(cpf.substring(0, 9), 10);
			int dig2 = calcularDigito(cpf.substring(0, 9) + dig1, 11);
			return cpf.equals(cpf.substring(0, 9) + dig1 + dig2);
		} catch (Exception ex) {
			return false;
		}
	}

	private int calcularDigito(String base, int pesoInicial) {
		int soma = 0;
		int peso = pesoInicial;
		for (char c : base.toCharArray()) {
			soma += Character.getNumericValue(c) * peso--;
		}
		int resto = soma % 11;
		return resto < 2 ? 0 : 11 - resto;
	}

	private String normalizarCpf(String cpf) {
		return cpf == null ? "" : cpf.replaceAll("\\D", "");
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private void registrarAtividade(String acao, String detalhe) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal principal) {
			auditoriaService.registrarAtividade(principal.getId(), principal.getUsername(), acao, detalhe);
		}
	}

	public Set<ModuloSistema> todosModulos() {
		return Arrays.stream(ModuloSistema.values()).collect(Collectors.toCollection(() -> EnumSet.noneOf(ModuloSistema.class)));
	}
}
