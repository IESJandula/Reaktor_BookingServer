package es.iesjandula.reaktor.bookings_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoFinal;

public interface IRecursoFinalRepository extends JpaRepository<RecursoFinal, String>
{
	@Query("SELECT r FROM RecursoFinal r WHERE " + "r.id = :recurso")
	Optional<RecursoFinal> encontrarRecurso(@Param("recurso") String recurso);
}
