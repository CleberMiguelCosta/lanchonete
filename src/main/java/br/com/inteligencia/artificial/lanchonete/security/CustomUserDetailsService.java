package br.com.inteligencia.artificial.lanchonete.security;

import br.com.inteligencia.artificial.lanchonete.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return usuarioRepository.findByUsernameIgnoreCase(username)
				.map(UsuarioPrincipal::new)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	}
}
