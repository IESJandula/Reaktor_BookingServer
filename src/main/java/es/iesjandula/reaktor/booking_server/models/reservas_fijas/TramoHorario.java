package es.iesjandula.reaktor.booking_server.models.reservas_fijas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase que representa un tramo horario fijo.
 * <p>
 * Cada tramo horario tiene un identificador único y una cadena que indica el
 * intervalo de tiempo, por ejemplo, "11:30/12:30".
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Data
@NoArgsConstructor
@Entity
public class TramoHorario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/**
	 * Intervalo de tiempo del tramo horario (por ejemplo "11:30/12:30").
	 */
	@Column(length = 20)
	private String tramoHorario;
}
