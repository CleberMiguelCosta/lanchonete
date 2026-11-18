package br.com.inteligencia.artificial.lanchonete.repository;

import br.com.inteligencia.artificial.lanchonete.model.Pedido;
import br.com.inteligencia.artificial.lanchonete.model.PedidoStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findAllByOrderByCriadoEmDesc();

	List<Pedido> findByCriadoEmBetweenOrderByCriadoEmDesc(LocalDateTime inicio, LocalDateTime fim);

	long countByStatusIn(List<PedidoStatus> status);

	long countByCriadoEmBetween(LocalDateTime inicio, LocalDateTime fim);
}
