package es.iesjandula.reaktor.bookings_server.models;

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
@Table(name= "costantes")
public class Constante 
{
	
	@Id
	private String clave;
	
	@Column
	private String valor;

}
