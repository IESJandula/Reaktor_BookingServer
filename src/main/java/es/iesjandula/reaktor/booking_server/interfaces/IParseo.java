package es.iesjandula.reaktor.booking_server.interfaces;

import java.util.Scanner;

import es.iesjandula.reaktor.booking_server.exception.ReservaException;

public interface IParseo
{

	void parseaFichero(Scanner scanner) throws ReservaException;

}
