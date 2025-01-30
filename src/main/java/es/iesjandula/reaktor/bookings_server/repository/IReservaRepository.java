package es.iesjandula.reaktor.bookings_server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.ReservaFija;
import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.ReservaFijaId;

public interface IReservaRepository extends JpaRepository<ReservaFija, ReservaFijaId>
{

//	Consulta que recupera la información sobre las reservas que están asociadas a 
//	un email, una aulaYCarritos, un diasDeLaSemana y un tramosHorarios
	@Query("SELECT r FROM ReservaFija r WHERE "
			+ "r.reservaFijaId.recursoPrevio.id = :recursoPrevio AND "
			+ "r.reservaFijaId.diaSemana.id = :diaSemana AND "
			+ "r.reservaFijaId.tramoHorario.id = :tramoHorario")
	Optional<ReservaFija> encontrarReserva(@Param("recursoPrevio") String recursoPrevio,
										   @Param("diaSemana") Long diaSemana,
										   @Param("tramoHorario") Long tramoHorario) ;
	
//	Consulta que recupera la información sobre las reservas que están asociadas a un recurso específico..
	@Query(value = 
		    "SELECT d.id, t.id, NULL, NULL, NULL, NULL "
		    + "FROM dia_semana d, tramo_horario t, reserva_fija r, profesor p "
		    + "WHERE ((d.id, t.id) NOT IN (SELECT r.dia_semana_id, r.tramo_horario_id FROM reserva_fija r)) "
		    + "UNION "
		    + "SELECT r2.dia_semana_id, r2.tramo_horario_id, r2.n_alumnos, r2.profesor_email, "
		    + "CONCAT(p.nombre, ' ', p.apellidos), r2.recurso_previo_id "
		    + "FROM reserva_fija r2, profesor p "
		    + "WHERE r2.profesor_email = p.email AND r2.recurso_previo_id = :recursoPrevio "
		    + "ORDER BY 1, 2", 
		    nativeQuery = true)
	List<Object[]> encontrarReservaPorRecurso(@Param("recursoPrevio") String recursoPrevio) ;
}
