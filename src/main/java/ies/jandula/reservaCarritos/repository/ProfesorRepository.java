package ies.jandula.reservaCarritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ies.jandula.reservaCarritos.models.Profesor;

public interface ProfesorRepository extends JpaRepository<Profesor, String>
{
}
