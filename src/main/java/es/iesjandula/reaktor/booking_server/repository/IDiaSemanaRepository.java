package es.iesjandula.reaktor.booking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;

/**
 * Repositorio para gestionar los días de la semana. Permite realizar
 * operaciones CRUD sobre los días de la semana.
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
public interface IDiaSemanaRepository extends JpaRepository<DiaSemana, String>
{

}
