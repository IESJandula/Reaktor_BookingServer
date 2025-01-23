package es.iesjandula.reaktor.booking_server.models.reservas_puntuales;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Holidays
{
	private String date;
	private String info;
}
