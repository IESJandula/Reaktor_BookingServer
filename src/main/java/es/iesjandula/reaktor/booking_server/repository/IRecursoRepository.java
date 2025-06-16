package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Recurso;

/**
 * Repositorio para manejar los recursos, con métodos para buscar recursos y sus
 * reservas.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public interface IRecursoRepository extends JpaRepository<Recurso, String>
{

	/**
	 * Encuentra un recurso por su id.
	 * 
	 * @param recurso id del recurso a buscar
	 * @return Optional con el recurso si existe
	 */
	@Query("SELECT r FROM Recurso r WHERE r.id = :recurso")
	Optional<Recurso> encontrarRecurso(@Param("recurso") String recurso);

	/**
	 * Busca todos los recursos que sean compartibles o no, según parámetro.
	 * 
	 * @param esCompartible true si quiere recursos compartibles, false si no
	 * @return lista de recursos que cumplen el criterio
	 */
	@Query("SELECT r FROM Recurso r WHERE r.esCompartible = :esCompartible")
	List<Recurso> encontrarRecursoCompartible(@Param("esCompartible") boolean esCompartible);

	/**
	 * Encuentra todos los motivos de reservas (fijas y temporales) asociadas a un
	 * recurso dado.
	 * 
	 * @param recurso id del recurso
	 * @return lista de motivos de curso relacionados con ese recurso
	 */
	@Query(value = "SELECT motivo_curso FROM reserva_fija WHERE recurso_id = :recurso UNION "
			+ "SELECT motivo_curso FROM reserva_temporal WHERE recurso_id = :recurso", nativeQuery = true)
	List<String> encontrarReservasPorRecurso(@Param("recurso") String recurso);
}
