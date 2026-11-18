package br.com.inteligencia.artificial.lanchonete.repository;

import br.com.inteligencia.artificial.lanchonete.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	List<Categoria> findAllByOrderByNomeAsc();

	boolean existsByNomeIgnoreCase(String nome);
}
