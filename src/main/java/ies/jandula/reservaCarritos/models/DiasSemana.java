package ies.jandula.reservaCarritos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class DiasSemana
{

	@Id
	@Column(length = 9)
	private String diasDeLaSemana;

}
