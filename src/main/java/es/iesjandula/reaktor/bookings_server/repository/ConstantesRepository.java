package es.iesjandula.reaktor.bookings_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.bookings_server.dto.DtoConstantes;
import es.iesjandula.reaktor.bookings_server.models.Constantes;

@Repository
public interface ConstantesRepository extends JpaRepository<Constantes, String>
{
	
	/**
     * Búsqueda de constante por clave
     * @param clave clave de la constante
     * @return constante encontrada
     */
	Optional<Constantes> findByClave(String clave);
	
	@Query("SELECT new es.iesjandula.reaktor.bookings_server.dto.DtoConstantes(c.clave, c.valor) "
			+ "FROM Constantes c")
	List<DtoConstantes> encontrarTodoComoDto();
	
}
