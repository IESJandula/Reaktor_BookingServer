package es.iesjandula.reaktor.booking_server.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
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
public interface LogReservasRepository extends JpaRepository<LogReservas, Date>
{

	/**
	 * Obtiene una lista paginada de logs de reservas ordenados por fecha
	 * descendente. Devuelve un máximo de 10 registros empezando desde el índice
	 * indicado.
	 * 
	 * @param inicio índice desde el que empezar a recuperar los logs (offset)
	 * @return lista con los logs de reservas paginados
	 */

	
	@Query(value = 
		"SELECT new es.iesjandula.reaktor.booking_server.models.LogReservas(l.id, l.usuario, l.accion, l.tipo, l.recurso, l.fechaReserva, l.diaSemana, l.tramoHorario, l.superUsuario) " +
		"FROM logReservas l " + 
		"ORDER BY l.fechaReserva DESC")
	Page<LogReservas> getPaginacionLogs(Pageable pageable);

	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto(l.recurso, COUNT(*)) " +
		       "FROM LogReservas l " +
		       "WHERE l.recurso IS NOT NULL AND l.recurso <> '' " +
		       "GROUP BY l.recurso " +
		       "ORDER BY COUNT(*) DESC")
		List<EstadisticaRecursoMasReservadoDto> obtenerRecursoMasReservado();

		@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaTramoMasReservadoDto(l.diaSemana, l.tramoHorario, COUNT(*)) " +
		       "FROM LogReservas l " +
		       "WHERE l.diaSemana IS NOT NULL AND l.diaSemana <> '' " +
		       "AND l.tramoHorario IS NOT NULL AND l.tramoHorario <> '' " +
		       "GROUP BY l.diaSemana, l.tramoHorario " +
		       "ORDER BY COUNT(*) DESC")
		List<EstadisticaDiaTramoMasReservadoDto> obtenerDiaTramoMasReservado();
}
