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
	private String usuario;

	@Column
	private String accion;
	
	@Column
	private String tipo;
	
	@Column
	private String recurso;
	
	@Column
	private String locReserva;
	
	@Column
	private String superusuario;
	
	@Column
	private Long numRegistro;
	
	@Column
	private Long countMax;

	public LogReservas(Date fecha, String usuario, String accion, String tipo, String recurso, String locReserva,
			String superusuario)
	{
		super();
		this.fecha = fecha;
		this.usuario = usuario;
		this.accion = accion;
		this.tipo = tipo;
		this.recurso = recurso;
		this.locReserva = locReserva;
		this.superusuario = superusuario;
	}
	
}
