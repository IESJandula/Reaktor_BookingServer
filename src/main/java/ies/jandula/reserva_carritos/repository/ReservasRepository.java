package ies.jandula.reserva_carritos.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ies.jandula.reserva_carritos.dto.ReservaDto;
import ies.jandula.reserva_carritos.models.reservas_fijas.RecursosPrevios;
import ies.jandula.reserva_carritos.models.reservas_fijas.ReservaFijas;
import ies.jandula.reserva_carritos.models.reservas_fijas.ReservasFijasId;

public interface ReservasRepository extends JpaRepository<ReservaFijas, ReservasFijasId>
{

//	Consulta que recupera la información sobre las reservas que están asociadas a 
//	un email, una aulaYCarritos, un diasDeLaSemana y un tramosHorarios
	@Query("SELECT r FROM ReservaFijas r WHERE r.reservaId.profesor.email = :email AND "
			+ "r.reservaId.aulaYCarritos.aulaYCarritos = :aulaYCarritos AND "
			+ "r.reservaId.diasDeLaSemana.diasDeLaSemana = :diasDeLaSemana AND "
			+ "r.reservaId.tramosHorarios.tramosHorarios = :tramosHorarios")
	Optional<ReservaFijas> encontrarReserva(@Param("email") String email, @Param("aulaYCarritos") String aulaYCarritos,
			@Param("diasDeLaSemana") String diasDeLaSemana, @Param("tramosHorarios") String tramosHorarios);

//	Consulta que recupera la información sobre las reservas que están asociadas a un recurso específico..
	@Query("SELECT new ies.jandula.reserva_carritos.dto.ReservaDto("
			+ "r.reservaId.diasDeLaSemana.diasDeLaSemana, r.reservaId.tramosHorarios.tramosHorarios, r.nAlumnos, p.email, CONCAT(p.nombre, ' ', p.apellidos)) "
			+ "FROM ReservaFijas r Join r.reservaId.profesor p "
			+ "WHERE r.reservaId.aulaYCarritos = :recurso")
	List<ReservaDto> encontrarReservaPorRecurso(@Param("recurso") RecursosPrevios recurso);

}
