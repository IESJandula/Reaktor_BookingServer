package es.iesjandula.reaktor.bookings_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursosPrevios;

public interface IRecursosRepository extends JpaRepository<RecursosPrevios, String>
{

}
