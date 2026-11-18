package br.com.inteligencia.artificial.lanchonete.repository;

import br.com.inteligencia.artificial.lanchonete.model.AtividadeLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtividadeLogRepository extends JpaRepository<AtividadeLog, Long> {

	List<AtividadeLog> findTop50ByOrderByDataHoraDesc();

	List<AtividadeLog> findTop30ByUsuarioIdOrderByDataHoraDesc(Long usuarioId);
}
