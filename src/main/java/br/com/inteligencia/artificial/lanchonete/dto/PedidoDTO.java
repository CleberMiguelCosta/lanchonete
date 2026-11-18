package br.com.inteligencia.artificial.lanchonete.dto;

import br.com.inteligencia.artificial.lanchonete.model.PedidoStatus;
import br.com.inteligencia.artificial.lanchonete.model.TipoAtendimento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoDTO {

	private Long id;
	private String numero;
	private Long lanchoneteId;
	private String lanchoneteNome;
	private Long clienteId;
	private String nomeCliente;
	private TipoAtendimento tipoAtendimento;
	private PedidoStatus status;
	private BigDecimal total;
	private String observacao;
	private LocalDateTime criadoEm;
	private List<PedidoItemDTO> itens = new ArrayList<>();

	public PedidoDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Long getLanchoneteId() {
		return lanchoneteId;
	}

	public void setLanchoneteId(Long lanchoneteId) {
		this.lanchoneteId = lanchoneteId;
	}

	public String getLanchoneteNome() {
		return lanchoneteNome;
	}

	public void setLanchoneteNome(String lanchoneteNome) {
		this.lanchoneteNome = lanchoneteNome;
	}

	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}

	public String getNomeCliente() {
		return nomeCliente;
	}

	public void setNomeCliente(String nomeCliente) {
		this.nomeCliente = nomeCliente;
	}

	public TipoAtendimento getTipoAtendimento() {
		return tipoAtendimento;
	}

	public void setTipoAtendimento(TipoAtendimento tipoAtendimento) {
		this.tipoAtendimento = tipoAtendimento;
	}

	public PedidoStatus getStatus() {
		return status;
	}

	public void setStatus(PedidoStatus status) {
		this.status = status;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public List<PedidoItemDTO> getItens() {
		return itens;
	}

	public void setItens(List<PedidoItemDTO> itens) {
		this.itens = itens;
	}
}
