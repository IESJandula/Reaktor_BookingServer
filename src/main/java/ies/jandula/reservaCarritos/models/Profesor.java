package ies.jandula.reservaCarritos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesor
{
	@Id
	@Column(length = 100)
	private String email;

	@Column(length = 100)
	private String nombre;

	@Column(length = 150)
	private String apellidos;

}
