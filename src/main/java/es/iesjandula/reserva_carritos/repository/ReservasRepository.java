package es.iesjandula.reserva_carritos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reserva_carritos.models.reservas_fijas.ReservaFijas;
import es.iesjandula.reserva_carritos.models.reservas_fijas.ReservasFijasId;

public interface ReservasRepository extends JpaRepository<ReservaFijas, ReservasFijasId>
{

//	Consulta que recupera la información sobre las reservas que están asociadas a 
//	un email, una aulaYCarritos, un diasDeLaSemana y un tramosHorarios
	@Query("SELECT r FROM ReservaFijas r WHERE "
			+ "r.reservaId.aulaYCarritos.aulaYCarritos = :aulaYCarritos AND "
			+ "r.reservaId.diasDeLaSemana.id = :diasDeLaSemana AND "
			+ "r.reservaId.tramosHorarios.id = :tramosHorarios")
	Optional<ReservaFijas> encontrarReserva( @Param("aulaYCarritos") String aulaYCarritos,
			@Param("diasDeLaSemana") Long diasDeLaSemana, @Param("tramosHorarios") Long tramosHorarios);

//	Consulta que recupera la información sobre las reservas que están asociadas a un recurso específico..
	@Query(value = 
		    "SELECT d.id, t.id, NULL, NULL, NULL, NULL "
		    + "FROM dias_semana d, tramos_horarios t, reserva_fijas r, profesores p "
		    + "WHERE ((d.id, t.id) NOT IN (SELECT r.dias_de_la_semana_id, r.tramos_horarios_id FROM reserva_fijas r)) "
		    + "UNION "
		    + "SELECT r2.dias_de_la_semana_id, r2.tramos_horarios_id, r2.n_alumnos, r2.profesor_email, "
		    + "CONCAT(p.nombre, ' ', p.apellidos), r2.recursos_aula_y_carritos "
		    + "FROM reserva_fijas r2, profesores p "
		    + "WHERE r2.profesor_email = p.email AND r2.recursos_aula_y_carritos = :recurso "
		    + "ORDER BY 1, 2", 
		    nativeQuery = true)
		List<Object[]> encontrarReservaPorRecurso(@Param("recurso") String recurso);
}
