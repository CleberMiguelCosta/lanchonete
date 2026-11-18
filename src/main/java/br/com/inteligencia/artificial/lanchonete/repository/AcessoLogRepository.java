package br.com.inteligencia.artificial.lanchonete.repository;

import br.com.inteligencia.artificial.lanchonete.model.AcessoLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcessoLogRepository extends JpaRepository<AcessoLog, Long> {

	List<AcessoLog> findTop50ByOrderByDataHoraDesc();

	List<AcessoLog> findTop30ByUsuarioIdOrderByDataHoraDesc(Long usuarioId);
}
