package es.iesjandula.reaktor.bookings_server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.Transactional;

import es.iesjandula.reaktor.bookings_server.interfaces.IGestorParseo;
import es.iesjandula.reaktor.bookings_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.bookings_server.repository.IProfesorRepository;
import es.iesjandula.reaktor.bookings_server.repository.IRecursoPrevioRepository;
import es.iesjandula.reaktor.bookings_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"es.iesjandula"})
public class ReaktorBookingServerApplication implements CommandLineRunner
{

	@Autowired
	private IGestorParseo iGestorParseo;

	@Autowired
	private IRecursoPrevioRepository recursosRepository;

	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository;

	@Autowired
	private IProfesorRepository profesoreRepository;

	@Autowired
	private IDiaSemanaRepository diasSemanaRepository;

	public static void main(String[] args)
	{
		SpringApplication.run(ReaktorBookingServerApplication.class, args);
	}

	@Transactional(readOnly = false)
	public void run(String... args) throws Exception
	{
		if (this.recursosRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Constants.FICHERO_RECURSO);
		}

		if (this.tramosHorariosRepository.findAll().isEmpty())
		{

			this.iGestorParseo.parseaFichero(Constants.FICHERO_TRAMOS_HORARIOS);
		}

		if (this.diasSemanaRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Constants.FICHERO_DIAS_SEMANAS);
		}
		
		if (this.profesoreRepository.findAll().isEmpty())
		{
			this.iGestorParseo.parseaFichero(Constants.FICHERO_PROFESORES);
		}
	}

}
