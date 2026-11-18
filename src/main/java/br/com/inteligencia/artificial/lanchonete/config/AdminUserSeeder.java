package br.com.inteligencia.artificial.lanchonete.config;

import br.com.inteligencia.artificial.lanchonete.model.ModuloSistema;
import br.com.inteligencia.artificial.lanchonete.model.PerfilUsuario;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import br.com.inteligencia.artificial.lanchonete.model.Usuario;
import br.com.inteligencia.artificial.lanchonete.repository.UsuarioRepository;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminUserSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (usuarioRepository.findByUsernameIgnoreCase("admin").isPresent()) {
			return;
		}
		Usuario admin = new Usuario();
		admin.setNomeCompleto("Administrador do Sistema");
		admin.setCpf("11144477735");
		admin.setEmail("admin@lanchonete.local");
		admin.setTelefone("11999990000");
		admin.setUsername("admin");
		admin.setSenhaHash(passwordEncoder.encode("admin123"));
		admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
		admin.setStatus(StatusUsuario.ATIVO);
		admin.setDeveAlterarSenha(false);
		admin.setPermissoes(EnumSet.allOf(ModuloSistema.class));
		usuarioRepository.save(admin);
		log.info("Usuário admin criado (login: admin / senha: admin123)");
	}
}
