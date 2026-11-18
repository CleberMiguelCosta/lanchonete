package br.com.inteligencia.artificial.lanchonete.security;

import br.com.inteligencia.artificial.lanchonete.model.ModuloSistema;
import br.com.inteligencia.artificial.lanchonete.model.PerfilUsuario;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import br.com.inteligencia.artificial.lanchonete.model.Usuario;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioPrincipal implements UserDetails {

	private final Long id;
	private final String username;
	private final String senhaHash;
	private final String nomeCompleto;
	private final String email;
	private final PerfilUsuario perfil;
	private final StatusUsuario status;
	private final boolean deveAlterarSenha;
	private final Set<ModuloSistema> permissoes;
	private final Collection<? extends GrantedAuthority> authorities;

	public UsuarioPrincipal(Usuario usuario) {
		this.id = usuario.getId();
		this.username = usuario.getUsername();
		this.senhaHash = usuario.getSenhaHash();
		this.nomeCompleto = usuario.getNomeCompleto();
		this.email = usuario.getEmail();
		this.perfil = usuario.getPerfil();
		this.status = usuario.getStatus();
		this.deveAlterarSenha = usuario.isDeveAlterarSenha();
		this.permissoes = new HashSet<>(usuario.getPermissoes());
		Set<GrantedAuthority> auths = new HashSet<>();
		auths.add(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));
		auths.addAll(usuario.getPermissoes().stream()
				.map(m -> new SimpleGrantedAuthority("MODULO_" + m.name()))
				.collect(Collectors.toSet()));
		this.authorities = auths;
	}

	public Long getId() {
		return id;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public String getEmail() {
		return email;
	}

	public PerfilUsuario getPerfil() {
		return perfil;
	}

	public StatusUsuario getStatus() {
		return status;
	}

	public boolean isDeveAlterarSenha() {
		return deveAlterarSenha;
	}

	public Set<ModuloSistema> getPermissoes() {
		return permissoes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return senhaHash;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return status == StatusUsuario.ATIVO;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return status == StatusUsuario.ATIVO;
	}
}
