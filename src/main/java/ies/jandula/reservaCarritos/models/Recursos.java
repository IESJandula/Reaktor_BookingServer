package ies.jandula.reservaCarritos.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Recursos 
{
	
	@Id
	@Column(length = 30)
	private String aulaYCarritos;
	
}