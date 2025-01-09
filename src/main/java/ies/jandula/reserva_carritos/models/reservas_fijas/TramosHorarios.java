package ies.jandula.reserva_carritos.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class TramosHorarios
{
	// 11:00/12:00
	@Id
	@Column(length = 20)
	private String tramosHorarios;

}
