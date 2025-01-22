package es.iesjandula.reaktor.booking_server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.iesjandula.reaktor.booking_server.dto.DtoConstante;
import es.iesjandula.reaktor.booking_server.models.Constante;

@Repository
public interface ConstanteRepository extends JpaRepository<Constante, String>
{
	
	@Query("SELECT new es.iesjandula.reaktor.booking_server.dto.DtoConstante(c.clave, c.valor) "
			+ "FROM Constante c")
	List<DtoConstante> encontrarTodoComoDto();
	
}
