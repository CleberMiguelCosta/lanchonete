package br.com.inteligencia.artificial.lanchonete.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nomeCompleto;

	@Column(nullable = false, unique = true, length = 14)
	private String cpf;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(length = 30)
	private String telefone;

	@Column(nullable = false, unique = true, length = 60)
	private String username;

	@Column(nullable = false, length = 100)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PerfilUsuario perfil;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatusUsuario status = StatusUsuario.ATIVO;

	@Column(nullable = false)
	private boolean deveAlterarSenha = true;

	@Column(nullable = false)
	private LocalDateTime criadoEm;

	private LocalDateTime ultimoAcesso;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_permissoes", joinColumns = @JoinColumn(name = "usuario_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "modulo", nullable = false, length = 40)
	private Set<ModuloSistema> permissoes = new HashSet<>();

	@PrePersist
	public void prePersist() {
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
		if (status == null) {
			status = StatusUsuario.ATIVO;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public PerfilUsuario getPerfil() {
		return perfil;
	}

	public void setPerfil(PerfilUsuario perfil) {
		this.perfil = perfil;
	}

	public StatusUsuario getStatus() {
		return status;
	}

	public void setStatus(StatusUsuario status) {
		this.status = status;
	}

	public boolean isDeveAlterarSenha() {
		return deveAlterarSenha;
	}

	public void setDeveAlterarSenha(boolean deveAlterarSenha) {
		this.deveAlterarSenha = deveAlterarSenha;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getUltimoAcesso() {
		return ultimoAcesso;
	}

	public void setUltimoAcesso(LocalDateTime ultimoAcesso) {
		this.ultimoAcesso = ultimoAcesso;
	}

	public Set<ModuloSistema> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(Set<ModuloSistema> permissoes) {
		this.permissoes = permissoes;
	}
}
