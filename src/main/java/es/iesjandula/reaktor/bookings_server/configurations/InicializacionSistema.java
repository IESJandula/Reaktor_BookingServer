package es.iesjandula.reaktor.bookings_server.configurations;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import es.iesjandula.reaktor.bookings_server.models.Constantes;
import es.iesjandula.reaktor.bookings_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.bookings_server.utils.Constants;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class InicializacionSistema
{

	@Autowired
	private ConstantesRepository constanteRepository;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String modoDdl;

	@Value("${" + Constants.PARAM_YAML_RESERVAS_FIJAS + "}")
	private String reservasFijas;

	@Value("${" + Constants.PARAM_YAML_RESERVAS_PUNTUALES + "}")
	private String reservasPuntuales;

	/**
	 * Este método se encarga de inicializar el sistema ya sea en el entorno de
	 * desarrollo o ejecutando JAR
	 */
	@PostConstruct
	public void inicializarSistema()
	{

		if (Constants.MODO_DDL_CREATE.equalsIgnoreCase(this.modoDdl))
		{
			this.inicializarSistemaConConstantes();

		}
	}

	/**
	 * Este método se encarga de inicializar el sistema con las constantes siempre
	 * que estemos creando la base de datos ya sea en el entorno de desarrollo o
	 * ejecutando JAR
	 */
	private void inicializarSistemaConConstantes()
	{
		this.cargarPropiedad(Constants.TABLA_CONST_RESERVAS_FIJAS, this.reservasFijas);
		this.cargarPropiedad(Constants.TABLA_CONST_RESERVAS_PUNTUALES, this.reservasPuntuales);
	}

	private void cargarPropiedad(String key, String value)
	{
		// Verificamos si tiene algún valor
		Optional<Constantes> property = this.constanteRepository.findById(key);

		// Si está vacío, lo seteamos con el valor del YAML
		if (property.isEmpty())
		{
			Constantes constante = new Constantes();

			constante.setClave(key);
			constante.setValor(value);

			// Almacenamos la constante en BBDD
			this.constanteRepository.save(constante);
		}
	}

}
