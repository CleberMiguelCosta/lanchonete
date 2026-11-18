package br.com.inteligencia.artificial.lanchonete.dto;

import br.com.inteligencia.artificial.lanchonete.model.ModuloSistema;
import br.com.inteligencia.artificial.lanchonete.model.PerfilUsuario;
import br.com.inteligencia.artificial.lanchonete.model.StatusUsuario;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class UsuarioDTO {

	private Long id;
	private String nomeCompleto;
	private String cpf;
	private String email;
	private String telefone;
	private String username;
	private String senha;
	private String confirmarSenha;
	private String senhaProvisoria;
	private boolean gerarSenhaProvisoria;
	private boolean enviarEmailAcesso = true;
	private PerfilUsuario perfil;
	private StatusUsuario status = StatusUsuario.ATIVO;
	private boolean deveAlterarSenha = true;
	private LocalDateTime criadoEm;
	private LocalDateTime ultimoAcesso;
	private Set<ModuloSistema> permissoes = new HashSet<>();

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

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getConfirmarSenha() {
		return confirmarSenha;
	}

	public void setConfirmarSenha(String confirmarSenha) {
		this.confirmarSenha = confirmarSenha;
	}

	public String getSenhaProvisoria() {
		return senhaProvisoria;
	}

	public void setSenhaProvisoria(String senhaProvisoria) {
		this.senhaProvisoria = senhaProvisoria;
	}

	public boolean isGerarSenhaProvisoria() {
		return gerarSenhaProvisoria;
	}

	public void setGerarSenhaProvisoria(boolean gerarSenhaProvisoria) {
		this.gerarSenhaProvisoria = gerarSenhaProvisoria;
	}

	public boolean isEnviarEmailAcesso() {
		return enviarEmailAcesso;
	}

	public void setEnviarEmailAcesso(boolean enviarEmailAcesso) {
		this.enviarEmailAcesso = enviarEmailAcesso;
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
