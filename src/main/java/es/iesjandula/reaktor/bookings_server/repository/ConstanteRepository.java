package es.iesjandula.reaktor.bookings_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.bookings_server.dto.DtoConstante;
import es.iesjandula.reaktor.bookings_server.models.Constante;

@Repository
public interface ConstanteRepository extends JpaRepository<Constante, String>
{
	
	/**
     * Búsqueda de constante por clave
     * @param clave clave de la constante
     * @return constante encontrada
     */
	Optional<Constante> findByClave(String clave);
	
	@Query("SELECT new es.iesjandula.reaktor.bookings_server.dto.DtoConstante(c.clave, c.valor) "
			+ "FROM Constante c")
	List<DtoConstante> encontrarTodoComoDto();
	
}
