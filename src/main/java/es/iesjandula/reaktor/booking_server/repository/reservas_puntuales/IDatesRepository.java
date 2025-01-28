package es.iesjandula.reaktor.booking_server.repository.reservas_puntuales;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reaktor.booking_server.models.reservas_puntuales.Dates;

public interface IDatesRepository extends JpaRepository<Dates, String> 
{

}
