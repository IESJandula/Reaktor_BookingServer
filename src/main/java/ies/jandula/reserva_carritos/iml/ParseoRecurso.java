package ies.jandula.reserva_carritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ies.jandula.reserva_carritos.exception.ReservaException;
import ies.jandula.reserva_carritos.interfaces.IParseoRecurso;
import ies.jandula.reserva_carritos.models.reservas_fijas.RecursosPrevios;
import ies.jandula.reserva_carritos.repository.RecursosRepository;

@Service
public class ParseoRecurso implements IParseoRecurso
{

	@Autowired
	private RecursosRepository recursosRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		// TODO Auto-generated method stub

		scanner.nextLine();

		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			RecursosPrevios recursos = new RecursosPrevios();

			recursos.setAulaYCarritos(lineaDelFicheroTroceada[0]);

			this.recursosRepository.saveAndFlush(recursos);
		}

	}

}
