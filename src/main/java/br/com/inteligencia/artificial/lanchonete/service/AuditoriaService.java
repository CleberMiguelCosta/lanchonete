package br.com.inteligencia.artificial.lanchonete.service;

import br.com.inteligencia.artificial.lanchonete.model.AcessoLog;
import br.com.inteligencia.artificial.lanchonete.model.AtividadeLog;
import br.com.inteligencia.artificial.lanchonete.repository.AcessoLogRepository;
import br.com.inteligencia.artificial.lanchonete.repository.AtividadeLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

	private final AcessoLogRepository acessoLogRepository;
	private final AtividadeLogRepository atividadeLogRepository;

	public AuditoriaService(AcessoLogRepository acessoLogRepository, AtividadeLogRepository atividadeLogRepository) {
		this.acessoLogRepository = acessoLogRepository;
		this.atividadeLogRepository = atividadeLogRepository;
	}

	public void registrarAcesso(Long usuarioId, String username, boolean sucesso, String ip, String detalhe) {
		AcessoLog log = new AcessoLog();
		log.setUsuarioId(usuarioId);
		log.setUsername(username);
		log.setSucesso(sucesso);
		log.setIp(ip);
		log.setDetalhe(detalhe);
		acessoLogRepository.save(log);
	}

	public void registrarAtividade(Long usuarioId, String username, String acao, String detalhe) {
		AtividadeLog log = new AtividadeLog();
		log.setUsuarioId(usuarioId);
		log.setUsername(username);
		log.setAcao(acao);
		log.setDetalhe(detalhe);
		atividadeLogRepository.save(log);
	}

	public List<AcessoLog> ultimosAcessos() {
		return acessoLogRepository.findTop50ByOrderByDataHoraDesc();
	}

	public List<AtividadeLog> ultimasAtividades() {
		return atividadeLogRepository.findTop50ByOrderByDataHoraDesc();
	}

	public List<AcessoLog> acessosDoUsuario(Long usuarioId) {
		return acessoLogRepository.findTop30ByUsuarioIdOrderByDataHoraDesc(usuarioId);
	}

	public List<AtividadeLog> atividadesDoUsuario(Long usuarioId) {
		return atividadeLogRepository.findTop30ByUsuarioIdOrderByDataHoraDesc(usuarioId);
	}
}
