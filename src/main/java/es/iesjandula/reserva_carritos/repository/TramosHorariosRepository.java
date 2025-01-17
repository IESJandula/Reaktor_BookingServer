package es.iesjandula.reserva_carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reserva_carritos.models.reservas_fijas.TramosHorarios;

public interface TramosHorariosRepository extends JpaRepository<TramosHorarios, String>
{

}
