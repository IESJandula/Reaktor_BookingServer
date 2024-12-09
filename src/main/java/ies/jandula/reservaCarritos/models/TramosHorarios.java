package ies.jandula.reservaCarritos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class TramosHorarios 
{
	//11:00/12:00
	 @Id
	 @Column(length = 20)
	 private String tramosHorarios;

	 
}
