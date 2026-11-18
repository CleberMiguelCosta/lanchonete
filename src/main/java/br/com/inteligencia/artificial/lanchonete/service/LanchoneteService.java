package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.LanchoneteDTO;
import br.com.inteligencia.artificial.lanchonete.model.Lanchonete;
import br.com.inteligencia.artificial.lanchonete.repository.LanchoneteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LanchoneteService {

	private final LanchoneteRepository lanchoneteRepository;
	private final LancheService lancheService;

	public LanchoneteService(LanchoneteRepository lanchoneteRepository, LancheService lancheService) {
		this.lanchoneteRepository = lanchoneteRepository;
		this.lancheService = lancheService;
	}

	public List<LanchoneteDTO> listarTodas() {
		return lanchoneteRepository.findAll().stream()
				.map(this::paraDTO)
				.toList();
	}

	public LanchoneteDTO buscarPorId(Long id) {
		Lanchonete lanchonete = buscarEntidadePorId(id);
		return paraDTO(lanchonete);
	}

	public LanchoneteDTO criar(LanchoneteDTO dto) {
		Lanchonete lanchonete = paraEntidade(dto);
		lanchonete.setId(null);
		Lanchonete salva = lanchoneteRepository.save(lanchonete);
		return paraDTO(salva);
	}

	public LanchoneteDTO atualizar(Long id, LanchoneteDTO dto) {
		Lanchonete lanchonete = buscarEntidadePorId(id);
		lanchonete.setNome(dto.getNome());
		lanchonete.setEndereco(dto.getEndereco());
		lanchonete.setCidade(dto.getCidade());
		Lanchonete atualizada = lanchoneteRepository.save(lanchonete);
		return paraDTO(atualizada);
	}

	@Transactional
	public void deletar(Long id) {
		if (!lanchoneteRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchonete não encontrada");
		}
		lancheService.deletarPorLanchonete(id);
		lanchoneteRepository.deleteById(id);
	}

	private Lanchonete buscarEntidadePorId(Long id) {
		return lanchoneteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchonete não encontrada"));
	}

	private LanchoneteDTO paraDTO(Lanchonete lanchonete) {
		return new LanchoneteDTO(
				lanchonete.getId(),
				lanchonete.getNome(),
				lanchonete.getEndereco(),
				lanchonete.getCidade()
		);
	}

	private Lanchonete paraEntidade(LanchoneteDTO dto) {
		return new Lanchonete(
				dto.getId(),
				dto.getNome(),
				dto.getEndereco(),
				dto.getCidade()
		);
	}
}
