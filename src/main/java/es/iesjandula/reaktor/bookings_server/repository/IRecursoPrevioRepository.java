package es.iesjandula.reaktor.bookings_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.bookings_server.models.reservas_fijas.RecursoPrevio;

public interface IRecursoPrevioRepository extends JpaRepository<RecursoPrevio, String>
{

}
