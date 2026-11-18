package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.CategoriaDTO;
import br.com.inteligencia.artificial.lanchonete.model.Categoria;
import br.com.inteligencia.artificial.lanchonete.repository.CategoriaRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;

	public CategoriaService(CategoriaRepository categoriaRepository) {
		this.categoriaRepository = categoriaRepository;
	}

	public List<CategoriaDTO> listar() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream().map(this::paraDTO).toList();
	}

	public CategoriaDTO buscar(Long id) {
		return paraDTO(buscarEntidade(id));
	}

	public CategoriaDTO criar(CategoriaDTO dto) {
		validar(dto);
		if (categoriaRepository.existsByNomeIgnoreCase(dto.getNome().trim())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma categoria com este nome");
		}
		Categoria categoria = new Categoria();
		aplicar(categoria, dto);
		return paraDTO(categoriaRepository.save(categoria));
	}

	public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
		validar(dto);
		Categoria categoria = buscarEntidade(id);
		aplicar(categoria, dto);
		return paraDTO(categoriaRepository.save(categoria));
	}

	public void deletar(Long id) {
		if (!categoriaRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada");
		}
		categoriaRepository.deleteById(id);
	}

	public Categoria buscarEntidade(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
	}

	private void validar(CategoriaDTO dto) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome da categoria é obrigatório");
		}
	}

	private void aplicar(Categoria categoria, CategoriaDTO dto) {
		categoria.setNome(dto.getNome().trim());
		categoria.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
		categoria.setAtiva(dto.isAtiva());
	}

	private CategoriaDTO paraDTO(Categoria categoria) {
		return new CategoriaDTO(categoria.getId(), categoria.getNome(), categoria.getDescricao(), categoria.isAtiva());
	}
}
