package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.dto.EstadisticaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.models.LogReservas;

/**
 * Repositorio para gestionar los logs de reservas.
 */
@Repository
public interface LogReservasRepository extends JpaRepository<LogReservas, Long>
{

	/**
	 * Obtiene una lista paginada de logs de reservas ordenados por fecha
	 * descendente. Devuelve la entidad LogReservas directamente (NO DTO).
	 */
	@Query("SELECT l FROM LogReservas l ORDER BY l.fechaReserva DESC")
	Page<LogReservas> getPaginacionLogs(Pageable pageable);

	/**
	 * Obtiene el recurso más reservado desde los logs.
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto(" + "  l.recurso, "
			+ "  COUNT(*) " + ") " + "FROM LogReservas l " + "WHERE l.recurso IS NOT NULL AND l.recurso <> '' "
			+ "GROUP BY l.recurso " + "ORDER BY COUNT(*) DESC")
	List<EstadisticaRecursoMasReservadoDto> obtenerRecursoMasReservado();

	/**
	 * Obtiene el día y tramo horario más reservado desde los logs.
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaTramoMasReservadoDto(" + "  l.diaSemana, "
			+ "  l.tramoHorario, " + "  COUNT(*) " + ") " + "FROM LogReservas l "
			+ "WHERE l.diaSemana IS NOT NULL AND l.diaSemana <> '' "
			+ "AND l.tramoHorario IS NOT NULL AND l.tramoHorario <> '' " + "GROUP BY l.diaSemana, l.tramoHorario "
			+ "ORDER BY COUNT(*) DESC")
	List<EstadisticaDiaTramoMasReservadoDto> obtenerDiaTramoMasReservado();
}