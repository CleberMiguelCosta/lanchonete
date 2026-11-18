package br.com.inteligencia.artificial.lanchonete.repository;

import br.com.inteligencia.artificial.lanchonete.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByUsernameIgnoreCase(String username);

	Optional<Usuario> findByEmailIgnoreCase(String email);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByCpf(String cpf);

	@Query("""
			SELECT u FROM Usuario u
			WHERE LOWER(u.nomeCompleto) LIKE LOWER(CONCAT('%', :q, '%'))
			   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
			   OR u.cpf LIKE CONCAT('%', :q, '%')
			   OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
			ORDER BY u.nomeCompleto ASC
			""")
	List<Usuario> buscar(@Param("q") String q);

	List<Usuario> findAllByOrderByNomeCompletoAsc();
}
