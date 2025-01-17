package es.iesjandula.reserva_carritos.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class RecursosPrevios
{

	@Id
	@Column(length = 30)
	private String aulaYCarritos;

}