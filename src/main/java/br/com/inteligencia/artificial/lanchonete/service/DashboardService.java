package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.DashboardDTO;
import br.com.inteligencia.artificial.lanchonete.dto.PedidoDTO;
import br.com.inteligencia.artificial.lanchonete.model.Pedido;
import br.com.inteligencia.artificial.lanchonete.model.PedidoItem;
import br.com.inteligencia.artificial.lanchonete.model.PedidoStatus;
import br.com.inteligencia.artificial.lanchonete.repository.PedidoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

	private static final List<PedidoStatus> EM_ANDAMENTO = List.of(
			PedidoStatus.AGUARDANDO,
			PedidoStatus.EM_PREPARACAO,
			PedidoStatus.PRONTO,
			PedidoStatus.SAIU_PARA_ENTREGA
	);

	private final PedidoRepository pedidoRepository;
	private final PedidoService pedidoService;

	public DashboardService(PedidoRepository pedidoRepository, PedidoService pedidoService) {
		this.pedidoRepository = pedidoRepository;
		this.pedidoService = pedidoService;
	}

	public DashboardDTO obter() {
		LocalDate hoje = LocalDate.now();
		LocalDate ontem = hoje.minusDays(1);

		List<Pedido> pedidosHoje = pedidosDoDia(hoje);
		List<Pedido> pedidosOntem = pedidosDoDia(ontem);
		List<Pedido> todos = pedidoRepository.findAllByOrderByCriadoEmDesc();

		DashboardDTO dto = new DashboardDTO();
		dto.setFaturamentoDia(somarConcluidos(pedidosHoje));
		dto.setFaturamentoOntem(somarConcluidos(pedidosOntem));
		dto.setQuantidadePedidos(pedidosHoje.size());
		dto.setPedidosOntem(pedidosOntem.size());
		dto.setTicketMedio(calcularTicket(pedidosHoje));
		dto.setPedidosEmAndamento(pedidoRepository.countByStatusIn(EM_ANDAMENTO));
		dto.setPedidosPorStatus(contarStatus(todos));
		dto.setFaturamentoSemana(montarSemana(hoje));
		dto.setProdutosMaisVendidos(topProdutos(todos));
		dto.setUltimosPedidos(todos.stream().limit(6).map(pedidoService::paraDTO).toList());
		return dto;
	}

	private List<Pedido> pedidosDoDia(LocalDate dia) {
		LocalDateTime inicio = dia.atStartOfDay();
		LocalDateTime fim = dia.atTime(LocalTime.MAX);
		return pedidoRepository.findByCriadoEmBetweenOrderByCriadoEmDesc(inicio, fim);
	}

	private BigDecimal somarConcluidos(List<Pedido> pedidos) {
		return pedidos.stream()
				.filter(p -> p.getStatus() != PedidoStatus.CANCELADO)
				.map(Pedido::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal calcularTicket(List<Pedido> pedidos) {
		List<Pedido> validos = pedidos.stream()
				.filter(p -> p.getStatus() != PedidoStatus.CANCELADO)
				.toList();
		if (validos.isEmpty()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}
		BigDecimal total = somarConcluidos(validos);
		return total.divide(BigDecimal.valueOf(validos.size()), 2, RoundingMode.HALF_UP);
	}

	private Map<String, Long> contarStatus(List<Pedido> pedidos) {
		Map<PedidoStatus, Long> contagem = new EnumMap<>(PedidoStatus.class);
		for (PedidoStatus status : PedidoStatus.values()) {
			contagem.put(status, 0L);
		}
		for (Pedido pedido : pedidos) {
			contagem.merge(pedido.getStatus(), 1L, Long::sum);
		}
		Map<String, Long> resultado = new LinkedHashMap<>();
		contagem.forEach((status, qtd) -> resultado.put(status.name(), qtd));
		return resultado;
	}

	private List<DashboardDTO.FaturamentoPontoDTO> montarSemana(LocalDate hoje) {
		List<DashboardDTO.FaturamentoPontoDTO> pontos = new ArrayList<>();
		for (int i = 6; i >= 0; i--) {
			LocalDate dia = hoje.minusDays(i);
			BigDecimal valor = somarConcluidos(pedidosDoDia(dia));
			String label = dia.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"));
			pontos.add(new DashboardDTO.FaturamentoPontoDTO(label, valor));
		}
		return pontos;
	}

	private List<DashboardDTO.ProdutoVendidoDTO> topProdutos(List<Pedido> pedidos) {
		Map<String, Long> mapa = new HashMap<>();
		for (Pedido pedido : pedidos) {
			if (pedido.getStatus() == PedidoStatus.CANCELADO) {
				continue;
			}
			for (PedidoItem item : pedido.getItens()) {
				mapa.merge(item.getNomeProduto(), item.getQuantidade().longValue(), Long::sum);
			}
		}
		return mapa.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
				.limit(5)
				.map(e -> new DashboardDTO.ProdutoVendidoDTO(e.getKey(), e.getValue()))
				.toList();
	}
}
