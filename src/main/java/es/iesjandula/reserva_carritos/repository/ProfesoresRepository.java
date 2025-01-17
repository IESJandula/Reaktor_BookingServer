package es.iesjandula.reserva_carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.iesjandula.reserva_carritos.models.reservas_fijas.Profesores;

public interface ProfesoresRepository extends JpaRepository<Profesores, String>
{
}
