package ies.jandula.reserva_carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ies.jandula.reserva_carritos.models.reservas_fijas.RecursosPrevios;

public interface RecursosRepository extends JpaRepository<RecursosPrevios, String>
{

}
