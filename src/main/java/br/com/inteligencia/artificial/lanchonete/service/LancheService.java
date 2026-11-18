package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.LancheDTO;
import br.com.inteligencia.artificial.lanchonete.model.Categoria;
import br.com.inteligencia.artificial.lanchonete.model.Lanche;
import br.com.inteligencia.artificial.lanchonete.model.Lanchonete;
import br.com.inteligencia.artificial.lanchonete.repository.LancheRepository;
import br.com.inteligencia.artificial.lanchonete.repository.LanchoneteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LancheService {

	private final LancheRepository lancheRepository;
	private final LanchoneteRepository lanchoneteRepository;
	private final CategoriaService categoriaService;

	public LancheService(
			LancheRepository lancheRepository,
			LanchoneteRepository lanchoneteRepository,
			CategoriaService categoriaService
	) {
		this.lancheRepository = lancheRepository;
		this.lanchoneteRepository = lanchoneteRepository;
		this.categoriaService = categoriaService;
	}

	public List<LancheDTO> listarTodos() {
		return lancheRepository.findAllByOrderByNomeAsc().stream().map(this::paraDTO).toList();
	}

	public List<LancheDTO> listarPorLanchonete(Long lanchoneteId) {
		garantirLanchoneteExiste(lanchoneteId);
		return lancheRepository.findByLanchoneteIdOrderByNomeAsc(lanchoneteId).stream()
				.map(this::paraDTO)
				.toList();
	}

	public LancheDTO buscarPorId(Long lanchoneteId, Long lancheId) {
		return paraDTO(buscarEntidade(lanchoneteId, lancheId));
	}

	public LancheDTO criar(Long lanchoneteId, LancheDTO dto) {
		Lanchonete lanchonete = garantirLanchoneteExiste(lanchoneteId);
		validarPayload(dto);

		Lanche lanche = new Lanche();
		aplicar(lanche, dto);
		lanche.setLanchonete(lanchonete);
		return paraDTO(lancheRepository.save(lanche));
	}

	public LancheDTO atualizar(Long lanchoneteId, Long lancheId, LancheDTO dto) {
		validarPayload(dto);
		Lanche lanche = buscarEntidade(lanchoneteId, lancheId);
		aplicar(lanche, dto);
		return paraDTO(lancheRepository.save(lanche));
	}

	public void deletar(Long lanchoneteId, Long lancheId) {
		Lanche lanche = buscarEntidade(lanchoneteId, lancheId);
		lancheRepository.delete(lanche);
	}

	@Transactional
	public void deletarPorLanchonete(Long lanchoneteId) {
		lancheRepository.deleteByLanchoneteId(lanchoneteId);
	}

	public Lanche buscarEntidadeGlobal(Long lancheId) {
		return lancheRepository.findById(lancheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanche não encontrado"));
	}

	private Lanche buscarEntidade(Long lanchoneteId, Long lancheId) {
		garantirLanchoneteExiste(lanchoneteId);
		Lanche lanche = buscarEntidadeGlobal(lancheId);
		if (!lanche.getLanchonete().getId().equals(lanchoneteId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanche não encontrado nesta lanchonete");
		}
		return lanche;
	}

	private Lanchonete garantirLanchoneteExiste(Long lanchoneteId) {
		return lanchoneteRepository.findById(lanchoneteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchonete não encontrada"));
	}

	private void aplicar(Lanche lanche, LancheDTO dto) {
		lanche.setNome(dto.getNome().trim());
		lanche.setDescricao(normalizar(dto.getDescricao()));
		lanche.setPreco(dto.getPreco());
		lanche.setImagemUrl(normalizar(dto.getImagemUrl()));
		lanche.setDisponivel(dto.isDisponivel());
		if (dto.getCategoriaId() != null) {
			Categoria categoria = categoriaService.buscarEntidade(dto.getCategoriaId());
			lanche.setCategoria(categoria);
		} else {
			lanche.setCategoria(null);
		}
	}

	private void validarPayload(LancheDTO dto) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do lanche é obrigatório");
		}
		if (dto.getPreco() == null || dto.getPreco().compareTo(BigDecimal.ZERO) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preço inválido");
		}
	}

	private String normalizar(String valor) {
		if (valor == null || valor.isBlank()) {
			return null;
		}
		return valor.trim();
	}

	private LancheDTO paraDTO(Lanche lanche) {
		LancheDTO dto = new LancheDTO();
		dto.setId(lanche.getId());
		dto.setNome(lanche.getNome());
		dto.setDescricao(lanche.getDescricao());
		dto.setPreco(lanche.getPreco());
		dto.setLanchoneteId(lanche.getLanchonete().getId());
		dto.setImagemUrl(lanche.getImagemUrl());
		dto.setDisponivel(lanche.isDisponivel());
		if (lanche.getCategoria() != null) {
			dto.setCategoriaId(lanche.getCategoria().getId());
			dto.setCategoriaNome(lanche.getCategoria().getNome());
		}
		return dto;
	}
}
