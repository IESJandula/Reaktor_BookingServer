package es.iesjandula.reaktor.booking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;

/**
 * Repositorio para gestionar los tramos horarios. Permite realizar operaciones
 * CRUD sobre los tramos horarios.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public interface ITramoHorarioRepository extends JpaRepository<TramoHorario, String> {

}
