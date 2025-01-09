package ies.jandula.reserva_carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ies.jandula.reserva_carritos.models.reservas_fijas.Profesores;

public interface ProfesoresRepository extends JpaRepository<Profesores, String>
{
}
