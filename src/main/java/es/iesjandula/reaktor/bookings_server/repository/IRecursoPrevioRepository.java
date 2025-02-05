package es.iesjandula.reaktor.bookings_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoPrevio;

public interface IRecursoPrevioRepository extends JpaRepository<RecursoPrevio, String>
{
	@Query("SELECT r FROM RecursoPrevio r WHERE " + "r.id = :recurso")
	Optional<RecursoPrevio> encontrarRecurso(@Param("recurso") String recurso);
}
