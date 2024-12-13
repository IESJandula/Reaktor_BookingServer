package ies.jandula.reservaCarritos.models;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Reserva 
{

	@EmbeddedId
	private ReservaId reservaId;
	
	@Column(nullable = false)
	private int nAlumnos;
	
	@Column(length = 100, nullable = false)
	private String nombreProfesor;
	
}