package es.iesjandula.reaktor.booking_server.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.models.LogReservas;

/**
 * Repositorio para gestionar los logs de reservas. Permite realizar operaciones
 * CRUD y obtener logs paginados.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Repository
public interface LogReservasRepository extends JpaRepository<LogReservas, Date> {

	/**
	 * Obtiene una lista paginada de logs de reservas ordenados por fecha
	 * descendente. Devuelve un máximo de 10 registros empezando desde el índice
	 * indicado.
	 * 
	 * @param inicio índice desde el que empezar a recuperar los logs (offset)
	 * @return lista con los logs de reservas paginados
	 */
	@Query(value = "SELECT " + "  ROW_NUMBER() OVER (ORDER BY fecha DESC) AS num_registro, "
			+ "  fecha, usuario, accion, tipo, recurso, loc_reserva, superusuario, " + "  COUNT(*) OVER() AS count_max "
			+ "FROM log_reservas " + "ORDER BY fecha DESC " + "LIMIT 10 OFFSET :inicio", nativeQuery = true)
	List<LogReservas> getPaginacionLogs(@Param("inicio") Integer inicio);

}
