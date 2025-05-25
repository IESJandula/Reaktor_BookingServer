package es.iesjandula.reaktor.booking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;

/**
 * Repositorio para manejar los profesores, permitiendo acceder y consultar
 * datos de los profesores.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public interface IProfesorRepository extends JpaRepository<Profesor, String> {

	/**
	 * Método que devuelve el nombre completo del profesor según su email.
	 * 
	 * @param email email del profesor
	 * @return nombre y apellidos concatenados del profesor
	 */
	@Query(value = "SELECT concat(nombre, ' ', apellidos) as Nombre FROM profesor where email = :email", nativeQuery = true)
	String getNombreProfesor(@Param("email") String email);

}
