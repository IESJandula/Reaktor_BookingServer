package es.iesjandula.reaktor.booking_server.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.booking_server.exception.ReservaException;
import es.iesjandula.reaktor.booking_server.interfaces.IParseoDiasSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiasSemana;
import es.iesjandula.reaktor.booking_server.repository.IDiasSemanaRepository;

@Service
public class ParseoDiasSemanas implements IParseoDiasSemana
{
	@Autowired
	private IDiasSemanaRepository diasSemanaRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{
		scanner.nextLine();

		while (scanner.hasNextLine())
		{
			String lineaDelFichero = scanner.nextLine();

			// Dividir la línea por comas
			String[] lineaDelFicheroTroceada = lineaDelFichero.split(",");

			DiasSemana diasSemana = new DiasSemana();
			diasSemana.setDiasDeLaSemana(lineaDelFicheroTroceada[0]);
			this.diasSemanaRepository.saveAndFlush(diasSemana);
		}
	}

}
