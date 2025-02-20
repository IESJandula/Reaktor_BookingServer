package es.iesjandula.reaktor.bookings_server.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecursoCantMaxDto
{

	private String recurso;

	private BigDecimal cantMax;

}
