package es.iesjandula.reaktor.bookings_server.interfaces;

import java.util.Scanner;

import es.iesjandula.reaktor.bookings_server.exception.ReservaException;

public interface IParseo
{

	void parseaFichero(Scanner scanner) throws ReservaException;

}
