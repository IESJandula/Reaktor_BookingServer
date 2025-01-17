package es.iesjandula.reserva_carritos.iml;

import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.iesjandula.reserva_carritos.exception.ReservaException;
import es.iesjandula.reserva_carritos.interfaces.IParseoTramoHorario;
import es.iesjandula.reserva_carritos.models.reservas_fijas.TramosHorarios;
import es.iesjandula.reserva_carritos.repository.TramosHorariosRepository;

@Service
public class ParseoTramoHorario implements IParseoTramoHorario
{

	@Autowired
	private TramosHorariosRepository tramoHorarioRepository;

	@Override
	public void parseaFichero(Scanner scanner) throws ReservaException
	{

		scanner.nextLine();

		while (scanner.hasNextLine())
		{

			String lineaDelFichero = scanner.nextLine();

			String[] valores = lineaDelFichero.split(",");

			TramosHorarios tramos = new TramosHorarios();

			tramos.setTramosHorarios(valores[0]);

			this.tramoHorarioRepository.saveAndFlush(tramos);
		}

	}

}
