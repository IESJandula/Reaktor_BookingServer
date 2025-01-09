package ies.jandula.reserva_carritos.interfaces;

import java.util.Scanner;

import ies.jandula.reserva_carritos.exception.ReservaException;

public interface IParseo
{

	void parseaFichero(Scanner scanner) throws ReservaException;

}
