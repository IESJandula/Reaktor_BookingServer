package es.iesjandula.reserva_carritos.interfaces;

import java.util.Scanner;

import es.iesjandula.reserva_carritos.exception.ReservaException;

public interface IParseo
{

	void parseaFichero(Scanner scanner) throws ReservaException;

}
