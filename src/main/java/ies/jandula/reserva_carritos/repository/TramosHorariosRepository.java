package ies.jandula.reserva_carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ies.jandula.reserva_carritos.models.reservas_fijas.TramosHorarios;

public interface TramosHorariosRepository extends JpaRepository<TramosHorarios, String>
{

}
