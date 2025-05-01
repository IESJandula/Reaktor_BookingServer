package es.iesjandula.reaktor.booking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;

public interface IProfesorRepository extends JpaRepository<Profesor, String>
{
	@Query(value = "SELECT concat(nombre, ' ', apellidos) as Nombre FROM profesor where email = :email", nativeQuery = true)
    String getNombreProfesor(@Param("email") String email);
}
