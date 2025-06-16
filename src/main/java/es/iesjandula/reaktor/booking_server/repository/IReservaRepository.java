package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFija;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.ReservaFijaId;

public interface IReservaRepository extends JpaRepository<ReservaFija, ReservaFijaId>
{

	/**
	 * Busca la reserva que coincide con el email, recurso, día de la semana y tramo
	 * horario.
	 * 
	 * @param email        correo del profesor
	 * @param recurso      id del recurso
	 * @param diaSemana    id del día de la semana
	 * @param tramoHorario id del tramo horario
	 * @return Optional con la reserva encontrada o vacío si no existe
	 */
	@Query("SELECT r FROM ReservaFija r WHERE " + "r.reservaFijaId.recurso.id = :recurso AND "
			+ "r.reservaFijaId.diaSemana.id = :diaSemana AND " + "r.reservaFijaId.tramoHorario.id = :tramoHorario AND "
			+ "r.reservaFijaId.profesor.email = :email")
	Optional<ReservaFija> encontrarReserva(@Param("email") String email, @Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario);

	/**
	 * Obtiene una lista con la información de reservas de un recurso específico,
	 * incluyendo franjas sin reserva y detalles de reservas existentes.
	 * 
	 * @param recurso id del recurso
	 * @return lista de arrays con datos variados sobre reservas y franjas libres
	 */
	@Query(value = "SELECT d.id, t.id, NULL, NULL, NULL, NULL, NULL "
			+ "FROM dia_semana d, tramo_horario t, reserva_fija r, profesor p "
			+ "WHERE ((d.id, t.id) NOT IN (SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_fija r)) " + "UNION "
			+ "SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, "
			+ "CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id, r2.motivo_curso "
			+ "FROM reserva_fija r2, profesor p " + "WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso "
			+ "ORDER BY 1, 2", nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso);

	/**
	 * Obtiene el número máximo de alumnos reservados para cada recurso, sumando
	 * alumnos por día y tramo horario.
	 * 
	 * @return lista con recurso y máximo de alumnos reservado
	 */
	@Query(value = "SELECT recurso_id, MAX(total_alumnos) AS max_alumnos "
			+ "FROM (SELECT recurso_id, dia_semana_id, tramo_horario_id, SUM(n_alumnos) AS total_alumnos FROM reserva_fija GROUP BY recurso_id, dia_semana_id, tramo_horario_id) AS Fija "
			+ "GROUP BY recurso_id", nativeQuery = true)
	List<Object[]> reservaFijaMax();
	
	
	/*
	 * Se obtiene la suma del número de alumnos para calcular la reserva máxima
	 */
	@Query("SELECT r FROM ReservaFija r WHERE " + "r.reservaFijaId.recurso.id = :recurso AND "
			+ "r.reservaFijaId.diaSemana.id = :diaSemana AND "
			+ "r.reservaFijaId.tramoHorario.id = :tramoHorario")
	Optional <List<ReservaFija>> encontrarReservasFijasPorDiaTramo(@Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario);

	/**
	 * Borra todas las reservas asociadas a un recurso dado.
	 * 
	 * @param recurso id del recurso del que borrar reservas
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM ReservaFija rt WHERE rt.reservaFijaId.recurso.id = :recurso")
	void deleteReservas(@Param("recurso") String recurso);
}
