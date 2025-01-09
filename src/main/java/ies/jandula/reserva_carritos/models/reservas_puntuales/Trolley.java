package ies.jandula.reserva_carritos.models.reservas_puntuales;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Manuel y Miguel
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 * trolley class
 */
public class Trolley extends Booking
{
	/**
	 * brand of the trolley
	 */
	private String brand;
	
	/**
	 * floor of the trolley
	 */
	private String floor;
	
	/**
	 * 
	 */
	private String deviceType;
	
	/**
	 * type of trolley
	 */
	private Integer trolleyNumber;

	public Trolley(Teacher teacher, String date, String brand, String floor, String deviceType, Integer trolleyNumber) 
	{
		super(teacher, date);
		this.brand = brand;
		this.floor = floor;
		this.deviceType = deviceType;
		this.trolleyNumber = trolleyNumber;
	}
	
	
}
