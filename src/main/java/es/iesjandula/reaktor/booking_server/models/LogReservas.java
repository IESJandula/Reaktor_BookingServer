package es.iesjandula.reaktor.booking_server.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log_reservas")
public class LogReservas
{

	@Id
	private Date fecha;

	@Column
	private String accion;
	
	@Column
	private String recurso;
	
	@Column
	private String profesor;
	
	@Column
	private String locReserva;
	
}
