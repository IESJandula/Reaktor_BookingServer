package es.iesjandula.reaktor.bookings_server.repository.reservas_puntuales;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.bookings_server.models.reservas_puntuales.ReservaPuntual;
import es.iesjandula.reaktor.bookings_server.models.reservas_puntuales.ReservaPuntualId;

public interface IReservaPuntualRepository extends JpaRepository<ReservaPuntual, ReservaPuntualId>
{

//	Consulta que recupera la información sobre las reservas que están asociadas a 
//	un email, una aulaYCarritos, un diasDeLaSemana y un tramosHorarios
	@Query("SELECT r FROM ReservaPuntual r WHERE " + "r.reservaPuntualId.recurso.id = :recurso AND "
			+ "r.reservaPuntualId.diaSemana.id = :diaSemana AND " + "r.reservaPuntualId.tramoHorario.id = :tramoHorario AND"
			+ " r.reservaPuntualId.profesor.email = :email and r.reservaPuntualId.numSemana = :numSemana")
	Optional<ReservaPuntual> encontrarReserva(@Param("email") String email, @Param("recurso") String recurso,
			@Param("diaSemana") Long diaSemana, @Param("tramoHorario") Long tramoHorario, @Param("numSemana") Integer numSemana);

//	Consulta que recupera la información sobre las reservas que están asociadas a un recurso específico..
	@Query(value = "SELECT d.id, t.id, NULL, NULL, NULL, NULL "
			+ "FROM dia_semana d, tramo_horario t, reserva_puntual r, profesor p "
			+ "WHERE ((d.id, t.id) NOT IN (SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_puntual r)) " + "UNION "
			+ "SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, "
			+ "CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_id " + "FROM reserva_puntual r2, profesor p "
			+ "WHERE r2.profesor_email = p.email AND r2.recurso_id = :recurso " + "ORDER BY 1, 2", nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso);
}
