package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class TramoHorario
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	// 11:30/12:30
	@Column(length = 20)
	private String tramoHorario;
}
