package br.com.inteligencia.artificial.lanchonete.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDTO {

	private BigDecimal faturamentoDia = BigDecimal.ZERO;
	private BigDecimal faturamentoOntem = BigDecimal.ZERO;
	private long quantidadePedidos;
	private long pedidosOntem;
	private BigDecimal ticketMedio = BigDecimal.ZERO;
	private long pedidosEmAndamento;
	private Map<String, Long> pedidosPorStatus = new LinkedHashMap<>();
	private List<FaturamentoPontoDTO> faturamentoSemana = new ArrayList<>();
	private List<ProdutoVendidoDTO> produtosMaisVendidos = new ArrayList<>();
	private List<PedidoDTO> ultimosPedidos = new ArrayList<>();

	public BigDecimal getFaturamentoDia() {
		return faturamentoDia;
	}

	public void setFaturamentoDia(BigDecimal faturamentoDia) {
		this.faturamentoDia = faturamentoDia;
	}

	public BigDecimal getFaturamentoOntem() {
		return faturamentoOntem;
	}

	public void setFaturamentoOntem(BigDecimal faturamentoOntem) {
		this.faturamentoOntem = faturamentoOntem;
	}

	public long getQuantidadePedidos() {
		return quantidadePedidos;
	}

	public void setQuantidadePedidos(long quantidadePedidos) {
		this.quantidadePedidos = quantidadePedidos;
	}

	public long getPedidosOntem() {
		return pedidosOntem;
	}

	public void setPedidosOntem(long pedidosOntem) {
		this.pedidosOntem = pedidosOntem;
	}

	public BigDecimal getTicketMedio() {
		return ticketMedio;
	}

	public void setTicketMedio(BigDecimal ticketMedio) {
		this.ticketMedio = ticketMedio;
	}

	public long getPedidosEmAndamento() {
		return pedidosEmAndamento;
	}

	public void setPedidosEmAndamento(long pedidosEmAndamento) {
		this.pedidosEmAndamento = pedidosEmAndamento;
	}

	public Map<String, Long> getPedidosPorStatus() {
		return pedidosPorStatus;
	}

	public void setPedidosPorStatus(Map<String, Long> pedidosPorStatus) {
		this.pedidosPorStatus = pedidosPorStatus;
	}

	public List<FaturamentoPontoDTO> getFaturamentoSemana() {
		return faturamentoSemana;
	}

	public void setFaturamentoSemana(List<FaturamentoPontoDTO> faturamentoSemana) {
		this.faturamentoSemana = faturamentoSemana;
	}

	public List<ProdutoVendidoDTO> getProdutosMaisVendidos() {
		return produtosMaisVendidos;
	}

	public void setProdutosMaisVendidos(List<ProdutoVendidoDTO> produtosMaisVendidos) {
		this.produtosMaisVendidos = produtosMaisVendidos;
	}

	public List<PedidoDTO> getUltimosPedidos() {
		return ultimosPedidos;
	}

	public void setUltimosPedidos(List<PedidoDTO> ultimosPedidos) {
		this.ultimosPedidos = ultimosPedidos;
	}

	public static class FaturamentoPontoDTO {
		private String dia;
		private BigDecimal valor = BigDecimal.ZERO;

		public FaturamentoPontoDTO() {
		}

		public FaturamentoPontoDTO(String dia, BigDecimal valor) {
			this.dia = dia;
			this.valor = valor;
		}

		public String getDia() {
			return dia;
		}

		public void setDia(String dia) {
			this.dia = dia;
		}

		public BigDecimal getValor() {
			return valor;
		}

		public void setValor(BigDecimal valor) {
			this.valor = valor;
		}
	}

	public static class ProdutoVendidoDTO {
		private String nome;
		private long quantidade;

		public ProdutoVendidoDTO() {
		}

		public ProdutoVendidoDTO(String nome, long quantidade) {
			this.nome = nome;
			this.quantidade = quantidade;
		}

		public String getNome() {
			return nome;
		}

		public void setNome(String nome) {
			this.nome = nome;
		}

		public long getQuantidade() {
			return quantidade;
		}

		public void setQuantidade(long quantidade) {
			this.quantidade = quantidade;
		}
	}
}
