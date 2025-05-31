package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.dto.DtoConstantes;
import es.iesjandula.reaktor.booking_server.models.Constantes;

/**
 * Repositorio para la gestión de las constantes de la aplicación. Permite
 * buscar constantes por clave y obtener todas las constantes en forma de DTO.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Repository
public interface ConstantesRepository extends JpaRepository<Constantes, String>
{

	/**
	 * Busca una constante según su clave.
	 * 
	 * @param clave la clave de la constante que quieres encontrar
	 * @return un Optional con la constante si existe
	 */
	Optional<Constantes> findByClave(String clave);

	/**
	 * Obtiene todas las constantes como lista de objetos DTO.
	 * 
	 * @return lista de constantes en formato DtoConstantes
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.DtoConstantes(c.clave, c.valor) FROM Constantes c")
	List<DtoConstantes> encontrarTodoComoDto();

}
