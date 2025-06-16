package es.iesjandula.reaktor.booking_server.repository.reservas_temporales;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
	 * @param email        email del profesor
	 * @param recurso      id del recurso
	 * @param diaSemana    id del día de la semana
	 * @param tramoHorario id del tramo horario
	 * @param numSemana    número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recurso AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemana AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorario AND "
			+ "r.reservaTemporalId.profesor.email = :email AND " + "r.reservaTemporalId.numSemana = :numSemana")
	Optional<ReservaTemporal> encontrarReserva(@Param("email") String email, @Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario,
			@Param("numSemana") Integer numSemana);

	/**
	 * Busca reservas temporales por recurso, día, tramo horario y número de semana,
	 * sin filtrar por profesor.
	 * 
	 * @param recurso      id del recurso
	 * @param diaSemana    id del día de la semana
	 * @param tramoHorario id del tramo horario
	 * @param numSemana    número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recurso AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemana AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorario AND " + "r.reservaTemporalId.numSemana = :numSemana")
	Optional <List<ReservaTemporal>> encontrarReservasPorDiaTramo(@Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario,
			@Param("numSemana") Integer numSemana);

	/**
	 * Busca reservas temporales no compartibles según recurso, día, tramo horario y
	 * número de semana.
	 * 
	 * @param recurso      id del recurso
	 * @param diaSemana    id del día de la semana
	 * @param tramoHorario id del tramo horario
	 * @param numSemana    número de la semana
	 * @return Optional con la reserva temporal si existe
	 */
	@Query("SELECT r FROM ReservaTemporal r WHERE " + "r.reservaTemporalId.recurso.id = :recurso AND "
			+ "r.reservaTemporalId.diaSemana.id = :diaSemana AND "
			+ "r.reservaTemporalId.tramoHorario.id = :tramoHorario AND " + "r.reservaTemporalId.numSemana = :numSemana")
	Optional<ReservaTemporal> encontrarReservaNoCompartible(@Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario,
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
	@Query(value = "SELECT d.id, t.id, NULL, NULL, NULL, NULL, NULL, NULL, " + "       CASE "
			+ "           WHEN (d.id, t.id) NOT IN ( "
			+ "               SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_fija r " + "               UNION "
			+ "               SELECT rt.dia_semana_id, rt.tramo_horario_id FROM reserva_temporal rt WHERE rt.num_semana = :numSemana "
			+ "           ) THEN NULL " + "           ELSE TRUE " + "       END AS es_reserva_fija "
			+ "FROM dia_semana d, tramo_horario t " + "WHERE ((d.id, t.id) NOT IN ( "
			+ "    SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_fija r " + "    UNION "
			+ "    SELECT rt.dia_semana_id, rt.tramo_horario_id FROM reserva_temporal rt WHERE rt.num_semana = :numSemana )) "
			+ "UNION " + "SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, "
			+ "CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id, TRUE, r2.motivo_curso, FALSE "
			+ "FROM reserva_fija r2, profesor p " + "WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso "
			+ "UNION " + "SELECT rt2.dia_semana_id, rt2.tramo_horario_id, rt2.n_alumnos, rt2.profesor_email, "
			+ "CONCAT(p.nombre, ' ', p.apellidos), rt2.recurso_id, NULL, rt2.motivo_curso, rt2.es_semanal "
			+ "FROM reserva_temporal rt2, profesor p "
			+ "WHERE rt2.profesor_email = p.email AND rt2.recurso_id = :recurso AND rt2.num_semana = :numSemana "
			+ "ORDER BY 1, 2", nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso, @Param("numSemana") Integer numSemana);

	/**
	 * Obtiene reservas temporales asociadas a un recurso, incluyendo combinaciones
	 * de días y tramos sin reservas.
	 * 
	 * @param recurso id del recurso
	 * @return lista de objetos con datos de reservas y días/tramos sin reserva
	 */
	@Query(value = "SELECT d.id, t.id, NULL, NULL, NULL, NULL "
			+ "FROM dia_semana d, tramo_horario t, reserva_temporal r, profesor p "
			+ "WHERE ((d.id, t.id) NOT IN (SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_temporal r)) "
			+ "UNION " + "SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, "
			+ "CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id " + "FROM reserva_temporal r2, profesor p "
			+ "WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso " + "ORDER BY 1, 2", nativeQuery = true)
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
	@Query(value = "Delete from ReservaTemporal rt where rt.reservaTemporalId.recurso.id = :recurso")
	void deleteReservas(@Param("recurso") String recurso);

}
