package ies.jandula.reservaCarritos.interfaces;

import java.util.Scanner;

import ies.jandula.reservaCarritos.exception.ReservaException;

public interface IParseo {
	
	void parseaFichero(Scanner scanner) throws ReservaException;

}
