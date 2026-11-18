package br.com.inteligencia.artificial.lanchonete.dto;

public class CategoriaDTO {

	private Long id;
	private String nome;
	private String descricao;
	private boolean ativa = true;

	public CategoriaDTO() {
	}

	public CategoriaDTO(Long id, String nome, String descricao, boolean ativa) {
		this.id = id;
		this.nome = nome;
		this.descricao = descricao;
		this.ativa = ativa;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public boolean isAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}
}
