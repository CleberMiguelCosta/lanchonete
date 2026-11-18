package br.com.inteligencia.artificial.lanchonete.dto;

import java.math.BigDecimal;

public class PedidoItemDTO {

	private Long id;
	private Long lancheId;
	private String nomeProduto;
	private Integer quantidade;
	private BigDecimal precoUnitario;

	public PedidoItemDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getLancheId() {
		return lancheId;
	}

	public void setLancheId(Long lancheId) {
		this.lancheId = lancheId;
	}

	public String getNomeProduto() {
		return nomeProduto;
	}

	public void setNomeProduto(String nomeProduto) {
		this.nomeProduto = nomeProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getPrecoUnitario() {
		return precoUnitario;
	}

	public void setPrecoUnitario(BigDecimal precoUnitario) {
		this.precoUnitario = precoUnitario;
	}
}
