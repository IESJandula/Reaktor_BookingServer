package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.dto.EstadisticaTramoMasReservadoDto;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporal;
import es.iesjandula.reaktor.booking_server.models.reservas_temporales.ReservaTemporalId;

/**
 * Repositorio para gestionar las reservas temporales. Proporciona métodos para
 * consultar reservas por profesor, recurso, día de la semana, tramo horario y
 * número de semana, así como para borrar reservas.
 * 
 * Permite obtener información detallada sobre las reservas temporales y su
 * relación con reservas fijas.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public interface IReservaTemporalRepository extends JpaRepository<ReservaTemporal, ReservaTemporalId>
{

	/**
	 * Busca una reserva temporal según profesor, recurso, día, tramo horario y
	 * número de semana.
	 * 
	 * @param email          email del profesor
	 * @param recursoId      id del recurso
	 * @param diaSemanaId    id del día de la semana
	 * @param tramoHorarioId id del tramo horario
	 * @param numSemana      número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recursoId AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemanaId AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorarioId AND "
			+ "r.reservaTemporalId.profesor.email = :email AND " + "r.reservaTemporalId.numSemana = :numSemana")
	Optional<ReservaTemporal> encontrarReserva(@Param("email") String email, @Param("recursoId") String recursoId,
			@Param("diaSemanaId") Long diaSemanaId, @Param("tramoHorarioId") Long tramoHorarioId,
			@Param("numSemana") Integer numSemana);

	/**
	 * Busca reservas temporales por recurso, día, tramo horario y número de semana,
	 * sin filtrar por profesor.
	 * 
	 * @param recursoId      id del recurso
	 * @param diaSemanaId    id del día de la semana
	 * @param tramoHorarioId id del tramo horario
	 * @param numSemana      número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recursoId AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemanaId AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorarioId AND "
			+ "r.reservaTemporalId.numSemana = :numSemana")
	Optional<List<ReservaTemporal>> encontrarReservasPorDiaTramo(@Param("recursoId") String recursoId,
			@Param("diaSemanaId") Long diaSemanaId, @Param("tramoHorarioId") Long tramoHorarioId,
			@Param("numSemana") Integer numSemana);

	/**
	 * Busca reservas temporales no compartibles según recurso, día, tramo horario y
	 * número de semana.
	 * 
	 * @param recursoId      id del recurso
	 * @param diaSemanaId    id del día de la semana
	 * @param tramoHorarioId id del tramo horario
	 * @param numSemana      número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recursoId AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemanaId AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorarioId AND "
			+ "r.reservaTemporalId.numSemana = :numSemana")
	Optional<ReservaTemporal> encontrarReservaNoCompartible(@Param("recursoId") String recursoId,
			@Param("diaSemanaId") Long diaSemanaId, @Param("tramoHorarioId") Long tramoHorarioId,
			@Param("numSemana") Integer numSemana);

	/**
	 * Obtiene reservas temporales y fijas relacionadas a un recurso y semana
	 * especificados, mostrando también las combinaciones de días y tramos sin
	 * reservas.
	 * 
	 * @param recurso   id del recurso
	 * @param numSemana número de la semana
	 * @return lista de objetos con datos de reservas y días/tramos sin reserva
	 */
	@Query(value = """
		    # ID Día, ID Tramo, N Alumnos, Email Profesor, Nombre Profesor, ID Recurso, Es Reserva Fija, Motivo Curso, Es Semanal
		    SELECT d.id, t.id, NULL, NULL, NULL, NULL, NULL, NULL, NULL
			FROM dia_semana d, tramo_horario t 
			WHERE ((d.id, t.id) NOT IN
					( 
			         SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_fija r
					 UNION 
			         SELECT rt.dia_semana_id, rt.tramo_horario_id FROM reserva_temporal rt WHERE rt.num_semana = :numSemana
					)
				   )
			
			UNION
		    
			# ID Día, ID Tramo, N Alumnos, Email Profesor, Nombre Profesor, ID Recurso, Es Reserva Fija, Motivo Curso, Es Semanal
			SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, 
				   CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id, 1, r2.motivo_curso, 0 
			FROM reserva_fija r2, profesor p
			WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso
			
			UNION
				
			# ID Día, ID Tramo, N Alumnos, Email, NombreApellidos, ID Recurso, Es Reserva Fija, Motivo, Es Semanal
			SELECT rt2.dia_semana_id, rt2.tramo_horario_id, rt2.n_alumnos, rt2.profesor_email,
			       CONCAT(p.nombre, ' ', p.apellidos), rt2.recurso_id, 0, rt2.motivo_curso, rt2.es_semanal
			FROM reserva_temporal rt2, profesor p
			WHERE rt2.profesor_email = p.email AND rt2.recurso_id = :recurso AND rt2.num_semana = :numSemana
			ORDER BY 1, 2
			""", nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso, @Param("numSemana") Integer numSemana);

	/**
	 * Obtiene reservas temporales asociadas a un recurso, incluyendo combinaciones
	 * de días y tramos sin reservas.
	 * 
	 * @param recurso id del recurso
	 * @return lista de objetos con datos de reservas y días/tramos sin reserva
	 */
	@Query(value = """
		   SELECT d.id, t.id, NULL, NULL, NULL, NULL
		   FROM dia_semana d, tramo_horario t, reserva_temporal r, profesor p
		   WHERE ((d.id, t.id) NOT IN (SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_temporal r))
		
		   UNION

		   # ID Día, ID Tramo, N Alumnos, Email Profesor, Nombre Profesor, ID Recurso
		   SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email,
			      CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id
		   FROM reserva_temporal r2, profesor p
		   WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso
		   ORDER BY 1, 2""", nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso);

	/**
	 * Obtiene el número máximo de alumnos por recurso en reservas temporales,
	 * agrupado por recurso.
	 * 
	 * @return lista con el id del recurso y el máximo número de alumnos
	 */
	@Query(value = "SELECT recurso_id, MAX(total_alumnos) AS max_alumnos "
			+ "FROM (SELECT recurso_id, dia_semana_id, tramo_horario_id, SUM(n_alumnos) AS total_alumnos FROM reserva_temporal GROUP BY recurso_id, dia_semana_id, tramo_horario_id) AS Puntual "
			+ "GROUP BY recurso_id", nativeQuery = true)
	List<Object[]> reservaTemporalMax();

	/**
	 * Elimina todas las reservas temporales asociadas a un recurso.
	 * 
	 * @param recurso id del recurso
	 */
	@Modifying
	@Transactional
	@Query(value = "Delete from ReservaTemporal rt where rt.reservaTemporalId.recurso.id = :recursoId")
	void deleteReservas(@Param("recursoId") String recursoId);

	/**
	 * Obtiene el recurso más reservado en reservas TEMPORALES activas.
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaRecursoMasReservadoDto("
			+ "   rt.reservaTemporalId.recurso.id, " + "   COUNT(rt)" + ") " + "FROM ReservaTemporal rt "
			+ "WHERE rt.reservaTemporalId.recurso.id IS NOT NULL " + "GROUP BY rt.reservaTemporalId.recurso.id "
			+ "ORDER BY COUNT(rt) DESC")
	List<EstadisticaRecursoMasReservadoDto> obtenerRecursoMasReservadoTemporal();

	/**
	 * Obtiene el día de la semana más reservado en reservas temporales activas.
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaDiaMasReservadoDto(" + "   ds.diaSemana, "
			+ "   COUNT(rt)" + ") " + "FROM ReservaTemporal rt " + "JOIN rt.reservaTemporalId.diaSemana ds "
			+ "WHERE ds.diaSemana IS NOT NULL " + "GROUP BY ds.diaSemana " + "ORDER BY COUNT(rt) DESC")
	List<EstadisticaDiaMasReservadoDto> obtenerDiaMasReservadoTemporal();

	/**
	 * Obtiene el tramo horario más reservado en reservas temporales activas.
	 */
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.EstadisticaTramoMasReservadoDto(" + "   ds.diaSemana, "
			+ "   th.tramoHorario, " + "   COUNT(rt)" + ") " + "FROM ReservaTemporal rt "
			+ "JOIN rt.reservaTemporalId.diaSemana ds " + "JOIN rt.reservaTemporalId.tramoHorario th "
			+ "WHERE th.tramoHorario IS NOT NULL " + "GROUP BY th.tramoHorario, ds.diaSemana "
			+ "ORDER BY COUNT(rt) DESC")
	List<EstadisticaTramoMasReservadoDto> obtenerTramoMasReservadoTemporal();
}
