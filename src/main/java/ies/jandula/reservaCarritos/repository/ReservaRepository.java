package ies.jandula.reservaCarritos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ies.jandula.reservaCarritos.dto.ReservaDto;
import ies.jandula.reservaCarritos.models.Recursos;
import ies.jandula.reservaCarritos.models.Reserva;
import ies.jandula.reservaCarritos.models.ReservaId;

public interface ReservaRepository extends JpaRepository<Reserva, ReservaId>
{

//	Consulta que recupera la información sobre las reservas que están asociadas a 
//	un email, una aulaYCarritos, un diasDeLaSemana y un tramosHorarios
	@Query("SELECT r FROM Reserva r WHERE r.reservaId.email = :email AND "
			+ "r.reservaId.aulaYCarritos.aulaYCarritos = :aulaYCarritos AND "
			+ "r.reservaId.diasDeLaSemana.diasDeLaSemana = :diasDeLaSemana AND "
			+ "r.reservaId.tramosHorarios.tramosHorarios = :tramosHorarios")
    Optional<Reserva> encontrarReserva(@Param("email") String email, 
    						 @Param("aulaYCarritos") String aulaYCarritos,
    						 @Param("diasDeLaSemana") String diasDeLaSemana,
    						 @Param("tramosHorarios") String tramosHorarios);
	

//	Consulta que recupera la información sobre las reservas que están asociadas a un recurso específico..
	@Query("SELECT new ies.jandula.reservaCarritos.dto.ReservaDto(" +
		   "r.reservaId.diasDeLaSemana.diasDeLaSemana, r.reservaId.tramosHorarios.tramosHorarios) " +
		   "FROM Reserva r " +
		   "WHERE r.reservaId.aulaYCarritos = :recurso")
	List<ReservaDto> encontrarReservaPorRecurso(@Param("recurso") Recursos recurso);

}
