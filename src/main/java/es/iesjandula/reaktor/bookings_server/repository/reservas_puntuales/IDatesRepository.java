package es.iesjandula.reaktor.bookings_server.repository.reservas_puntuales;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.bookings_server.models.reservas_puntuales.Dates;

public interface IDatesRepository extends JpaRepository<Dates, String> 
{

}
