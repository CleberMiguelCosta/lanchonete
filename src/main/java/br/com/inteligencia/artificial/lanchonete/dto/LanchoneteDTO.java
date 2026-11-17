package br.com.inteligencia.artificial.lanchonete.dto;

public class LanchoneteDTO {

	private Long id;
	private String nome;
	private String endereco;
	private String cidade;

	public LanchoneteDTO() {
	}

	public LanchoneteDTO(Long id, String nome, String endereco, String cidade) {
		this.id = id;
		this.nome = nome;
		this.endereco = endereco;
		this.cidade = cidade;
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

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getCidade() {
		return cidade;
	}

	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
}
