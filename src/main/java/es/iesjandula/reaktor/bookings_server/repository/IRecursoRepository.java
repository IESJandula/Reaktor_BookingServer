package es.iesjandula.reaktor.bookings_server.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.Recurso;

public interface IRecursoRepository extends JpaRepository<Recurso, String>
{
	@Query("SELECT r FROM Recurso r WHERE " + "r.id = :recurso")
	Optional<Recurso> encontrarRecurso(@Param("recurso") String recurso);
}
