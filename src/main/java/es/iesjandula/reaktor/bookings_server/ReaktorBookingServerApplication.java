package es.iesjandula.reaktor.bookings_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.bookings_server.interfaces.IGestorParseo;
import es.iesjandula.reaktor.bookings_server.models.Constantes;
import es.iesjandula.reaktor.bookings_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.bookings_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.bookings_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages =
{ "es.iesjandula" })
public class ReaktorBookingServerApplication implements CommandLineRunner
{

	@Autowired
	private IGestorParseo iGestorParseo;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	@Autowired
	private ConstantesRepository constantesRepository;

	public static void main(String[] args)
	{
		SpringApplication.run(ReaktorBookingServerApplication.class, args);
	}

	@Transactional(readOnly = false)
	public void run(String... args) throws Exception
	{
		if (this.tramosHorariosRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Constants.FICHERO_TRAMOS_HORARIOS);
		}

		if (this.diasSemanaRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Constants.FICHERO_DIAS_SEMANAS);
		}

		Constantes constantes1 = new Constantes("Reservas fijas", "");
		Constantes constantes2 = new Constantes("Reservas temporales", "");

		constantesRepository.saveAndFlush(constantes1);
		constantesRepository.saveAndFlush(constantes2);
	}

}
