package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.PedidoDTO;
import br.com.inteligencia.artificial.lanchonete.dto.PedidoItemDTO;
import br.com.inteligencia.artificial.lanchonete.model.Cliente;
import br.com.inteligencia.artificial.lanchonete.model.Lanche;
import br.com.inteligencia.artificial.lanchonete.model.Lanchonete;
import br.com.inteligencia.artificial.lanchonete.model.Pedido;
import br.com.inteligencia.artificial.lanchonete.model.PedidoItem;
import br.com.inteligencia.artificial.lanchonete.model.PedidoStatus;
import br.com.inteligencia.artificial.lanchonete.model.TipoAtendimento;
import br.com.inteligencia.artificial.lanchonete.repository.LanchoneteRepository;
import br.com.inteligencia.artificial.lanchonete.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {

	private static final AtomicInteger SEQUENCIA = new AtomicInteger(1);
	private static final DateTimeFormatter NUMERO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final PedidoRepository pedidoRepository;
	private final LanchoneteRepository lanchoneteRepository;
	private final ClienteService clienteService;
	private final LancheService lancheService;

	public PedidoService(
			PedidoRepository pedidoRepository,
			LanchoneteRepository lanchoneteRepository,
			ClienteService clienteService,
			LancheService lancheService
	) {
		this.pedidoRepository = pedidoRepository;
		this.lanchoneteRepository = lanchoneteRepository;
		this.clienteService = clienteService;
		this.lancheService = lancheService;
	}

	public List<PedidoDTO> listar() {
		return pedidoRepository.findAllByOrderByCriadoEmDesc().stream().map(this::paraDTO).toList();
	}

	public PedidoDTO buscar(Long id) {
		return paraDTO(buscarEntidade(id));
	}

	@Transactional
	public PedidoDTO criar(PedidoDTO dto) {
		if (dto.getLanchoneteId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lanchonete é obrigatória");
		}
		if (dto.getTipoAtendimento() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de atendimento é obrigatório");
		}
		if (dto.getItens() == null || dto.getItens().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe ao menos um item");
		}

		Lanchonete lanchonete = lanchoneteRepository.findById(dto.getLanchoneteId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchonete não encontrada"));

		Pedido pedido = new Pedido();
		pedido.setNumero(gerarNumero());
		pedido.setLanchonete(lanchonete);
		pedido.setTipoAtendimento(dto.getTipoAtendimento());
		pedido.setStatus(dto.getStatus() == null ? PedidoStatus.AGUARDANDO : dto.getStatus());
		pedido.setObservacao(blankToNull(dto.getObservacao()));

		if (dto.getClienteId() != null) {
			Cliente cliente = clienteService.buscarEntidade(dto.getClienteId());
			pedido.setCliente(cliente);
			pedido.setNomeCliente(cliente.getNome());
		} else {
			if (dto.getNomeCliente() == null || dto.getNomeCliente().isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do cliente é obrigatório");
			}
			pedido.setNomeCliente(dto.getNomeCliente().trim());
		}

		BigDecimal total = BigDecimal.ZERO;
		for (PedidoItemDTO itemDto : dto.getItens()) {
			PedidoItem item = montarItem(itemDto, lanchonete.getId());
			item.setPedido(pedido);
			pedido.getItens().add(item);
			total = total.add(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
		}
		pedido.setTotal(total);

		return paraDTO(pedidoRepository.save(pedido));
	}

	@Transactional
	public PedidoDTO atualizarStatus(Long id, PedidoStatus status) {
		if (status == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status é obrigatório");
		}
		Pedido pedido = buscarEntidade(id);
		pedido.setStatus(status);
		return paraDTO(pedidoRepository.save(pedido));
	}

	public void deletar(Long id) {
		if (!pedidoRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado");
		}
		pedidoRepository.deleteById(id);
	}

	public Pedido buscarEntidade(Long id) {
		return pedidoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
	}

	private PedidoItem montarItem(PedidoItemDTO itemDto, Long lanchoneteId) {
		if (itemDto.getQuantidade() == null || itemDto.getQuantidade() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade inválida");
		}

		PedidoItem item = new PedidoItem();
		item.setQuantidade(itemDto.getQuantidade());

		if (itemDto.getLancheId() != null) {
			Lanche lanche = lancheService.buscarEntidadeGlobal(itemDto.getLancheId());
			if (!lanche.getLanchonete().getId().equals(lanchoneteId)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto não pertence à lanchonete");
			}
			item.setLanche(lanche);
			item.setNomeProduto(lanche.getNome());
			item.setPrecoUnitario(lanche.getPreco());
		} else {
			if (itemDto.getNomeProduto() == null || itemDto.getNomeProduto().isBlank()) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do produto é obrigatório");
			}
			if (itemDto.getPrecoUnitario() == null || itemDto.getPrecoUnitario().compareTo(BigDecimal.ZERO) < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço do item inválido");
			}
			item.setNomeProduto(itemDto.getNomeProduto().trim());
			item.setPrecoUnitario(itemDto.getPrecoUnitario());
		}
		return item;
	}

	private String gerarNumero() {
		return "P" + LocalDateTime.now().format(NUMERO_FMT) + String.format("%03d", SEQUENCIA.getAndIncrement() % 1000);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	public PedidoDTO paraDTO(Pedido pedido) {
		PedidoDTO dto = new PedidoDTO();
		dto.setId(pedido.getId());
		dto.setNumero(pedido.getNumero());
		dto.setLanchoneteId(pedido.getLanchonete().getId());
		dto.setLanchoneteNome(pedido.getLanchonete().getNome());
		if (pedido.getCliente() != null) {
			dto.setClienteId(pedido.getCliente().getId());
		}
		dto.setNomeCliente(pedido.getNomeCliente());
		dto.setTipoAtendimento(pedido.getTipoAtendimento());
		dto.setStatus(pedido.getStatus());
		dto.setTotal(pedido.getTotal());
		dto.setObservacao(pedido.getObservacao());
		dto.setCriadoEm(pedido.getCriadoEm());
		dto.setItens(pedido.getItens().stream().map(this::paraItemDTO).toList());
		return dto;
	}

	private PedidoItemDTO paraItemDTO(PedidoItem item) {
		PedidoItemDTO dto = new PedidoItemDTO();
		dto.setId(item.getId());
		if (item.getLanche() != null) {
			dto.setLancheId(item.getLanche().getId());
		}
		dto.setNomeProduto(item.getNomeProduto());
		dto.setQuantidade(item.getQuantidade());
		dto.setPrecoUnitario(item.getPrecoUnitario());
		return dto;
	}
}
