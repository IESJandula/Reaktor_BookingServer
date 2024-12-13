package ies.jandula.reservaCarritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ies.jandula.reservaCarritos.models.TramosHorarios;

public interface TramoHorarioRepository extends JpaRepository<TramosHorarios, String>
{

}
