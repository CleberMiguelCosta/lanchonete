package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.dto.ClienteDTO;
import br.com.inteligencia.artificial.lanchonete.model.Cliente;
import br.com.inteligencia.artificial.lanchonete.repository.ClienteRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClienteService {

	private final ClienteRepository clienteRepository;

	public ClienteService(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	public List<ClienteDTO> listar() {
		return clienteRepository.findAllByOrderByNomeAsc().stream().map(this::paraDTO).toList();
	}

	public ClienteDTO buscar(Long id) {
		return paraDTO(buscarEntidade(id));
	}

	public ClienteDTO criar(ClienteDTO dto) {
		validar(dto);
		Cliente cliente = new Cliente();
		aplicar(cliente, dto);
		return paraDTO(clienteRepository.save(cliente));
	}

	public ClienteDTO atualizar(Long id, ClienteDTO dto) {
		validar(dto);
		Cliente cliente = buscarEntidade(id);
		aplicar(cliente, dto);
		return paraDTO(clienteRepository.save(cliente));
	}

	public void deletar(Long id) {
		if (!clienteRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado");
		}
		clienteRepository.deleteById(id);
	}

	public Cliente buscarEntidade(Long id) {
		return clienteRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
	}

	private void validar(ClienteDTO dto) {
		if (dto.getNome() == null || dto.getNome().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome do cliente é obrigatório");
		}
	}

	private void aplicar(Cliente cliente, ClienteDTO dto) {
		cliente.setNome(dto.getNome().trim());
		cliente.setTelefone(blankToNull(dto.getTelefone()));
		cliente.setEmail(blankToNull(dto.getEmail()));
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private ClienteDTO paraDTO(Cliente cliente) {
		return new ClienteDTO(cliente.getId(), cliente.getNome(), cliente.getTelefone(), cliente.getEmail());
	}
}
