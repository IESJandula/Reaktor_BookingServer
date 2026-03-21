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
}