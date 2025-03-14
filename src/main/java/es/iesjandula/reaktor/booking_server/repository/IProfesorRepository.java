package es.iesjandula.reaktor.booking_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.booking_server.models.reservas_fijas.Profesor;

public interface IProfesorRepository extends JpaRepository<Profesor, String>
{
}
