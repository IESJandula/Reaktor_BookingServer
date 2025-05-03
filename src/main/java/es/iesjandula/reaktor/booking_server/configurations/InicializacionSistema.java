package es.iesjandula.reaktor.booking_server.configurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import es.iesjandula.reaktor.base.resources_handler.ResourcesHandler;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerFile;
import es.iesjandula.reaktor.base.resources_handler.ResourcesHandlerJar;
import es.iesjandula.reaktor.base.utils.BaseException;
import es.iesjandula.reaktor.booking_server.exception.BookingError;
import es.iesjandula.reaktor.booking_server.models.Constantes;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.DiaSemana;
import es.iesjandula.reaktor.booking_server.models.reservas_fijas.TramoHorario;
import es.iesjandula.reaktor.booking_server.repository.ConstantesRepository;
import es.iesjandula.reaktor.booking_server.repository.IDiaSemanaRepository;
import es.iesjandula.reaktor.booking_server.repository.ITramoHorarioRepository;
import es.iesjandula.reaktor.booking_server.utils.Constants;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class InicializacionSistema
{
	@Autowired
	private ITramoHorarioRepository tramosHorariosRepository ;
	
	@Autowired
	private IDiaSemanaRepository diaSemanaRepository ;

	@Autowired
	private ConstantesRepository constantesRepository ;

	@Value("${reaktor.reiniciarParametros}")
	private boolean reiniciarParametros;

	@Value("${" + Constants.PARAM_YAML_RESERVAS_FIJAS + "}")
	private String reservasFijas ;

	@Value("${" + Constants.PARAM_YAML_RESERVAS_TEMPORALES + "}")
	private String reservasTemporales ;
	
	@Value("${" + Constants.PARAM_YAML_MAX_CALENDARIO + "}")
	private String maxDiasCalendario;

	/**
	 * Este método se encarga de inicializar el sistema ya sea en el entorno de
	 * desarrollo o ejecutando JAR
	 * @throws BaseException con un error
	 * @throws BookingError con un error
	 */
	@PostConstruct
	public void inicializarSistema() throws BaseException, BookingError
	{
		// Esta es la carpeta con las subcarpetas y configuraciones
	    ResourcesHandler bookingServerConfig = this.getResourcesHandler(Constants.BOOKING_SERVER_CONFIG);
	    
	    if (bookingServerConfig != null)
	    {
	    	// Nombre de la carpeta destino
	    	File bookingServerConfigExec = new File(Constants.BOOKING_SERVER_CONFIG_EXEC) ;
	    	
	    	// Copiamos las plantillas (origen) al destino
	    	bookingServerConfig.copyToDirectory(bookingServerConfigExec) ;
	    }
		
		if (this.reiniciarParametros)
		{
			// Parseamos los tramos horarios
			this.cargarTramosHorariosDesdeCSVInternal() ;

			// Parseamos los días de la semana
			this.cargarDiasSemanaDesdeCSVInternal() ;

			// Inicializamos el sistema con las constantes
			this.inicializarSistemaConConstantes();
		}
	}
	
	/**
	 * 
	 * @param resourceFilePath con la carpeta origen que tiene las plantillas
	 * @return el manejador que crea la estructura
	 */
	private ResourcesHandler getResourcesHandler(String resourceFilePath)
	{
		ResourcesHandler outcome = null;

		URL baseDirSubfolderUrl = Thread.currentThread().getContextClassLoader().getResource(resourceFilePath);
		if (baseDirSubfolderUrl != null)
		{
			if (baseDirSubfolderUrl.getProtocol().equalsIgnoreCase("file"))
			{
				outcome = new ResourcesHandlerFile(baseDirSubfolderUrl);
			}
			else
			{
				outcome = new ResourcesHandlerJar(baseDirSubfolderUrl);
			}
		}
		
		return outcome;
	}
	
    /**
     * Carga tramos horarios desde CSV - Internal
     * @throws BookingError excepción mientras se leían los tramos horarios
     */
	private void cargarTramosHorariosDesdeCSVInternal() throws BookingError
	{
    	// Inicializamos la lista de tramos horarios
        List<TramoHorario> tramosHorarios = new ArrayList<TramoHorario>() ;
        
        BufferedReader reader = null ;

        try
        {
            // Leer el archivo CSV desde la carpeta de recursos
            reader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_TRAMOS_HORARIOS), Charset.forName("UTF-8"))) ;
            
            // Nos saltamos la primera línea
            reader.readLine() ;

            // Leemos la segunda línea que ya tiene datos
            String linea = reader.readLine() ;
            
            while (linea != null)
            {
            	// Leemos la línea y la spliteamos
                String[] valores = linea.split(",") ;

    			TramoHorario tramos = new TramoHorario();
    			tramos.setTramoHorario(valores[0]);
    			
    			// Añadimos a la lista
    			tramosHorarios.add(tramos) ;
                
                // Leemos la siguiente línea
                linea = reader.readLine() ;
            }
        }
        catch (IOException ioException)
        {
			String errorString = "IOException mientras se leía línea de tramo horario" ;
			
			log.error(errorString, ioException) ;
			throw new BookingError(Constants.ERR_CODE_PROCESANDO_TRAMO_HORARIO, errorString, ioException) ;
        }
        finally
        {
        	this.cerrarFlujo(reader) ;
        }

        // Guardamos los tramos horarios en la base de datos
        if (!tramosHorarios.isEmpty())
        {
            this.tramosHorariosRepository.saveAllAndFlush(tramosHorarios) ;
        }
	}
	
    /**
     * Carga días semana desde CSV - Internal
     * @throws BookingError excepción mientras se leían los días de la semana
     */
	private void cargarDiasSemanaDesdeCSVInternal() throws BookingError
	{
    	// Inicializamos la lista de días de la semana
        List<DiaSemana> diasSemana = new ArrayList<DiaSemana>() ;
        
        BufferedReader reader = null ;

        try
        {
            // Leer el archivo CSV desde la carpeta de recursos
            reader = new BufferedReader(new FileReader(ResourceUtils.getFile(Constants.FICHERO_DIAS_SEMANAS), Charset.forName("UTF-8"))) ;
            
            // Nos saltamos la primera línea
            reader.readLine() ;

            // Leemos la segunda línea que ya tiene datos
            String linea = reader.readLine() ;
            
            while (linea != null)
            {
            	// Leemos la línea y la spliteamos
                String[] valores = linea.split(",") ;

    			DiaSemana diaSemana = new DiaSemana();
    			diaSemana.setDiaSemana(valores[0]);
    			
    			// Añadimos a la lista
    			diasSemana.add(diaSemana) ;
                
                // Leemos la siguiente línea
                linea = reader.readLine() ;
            }
        }
        catch (IOException ioException)
        {
			String errorString = "IOException mientras se leía línea de dia de la semana" ;
			
			log.error(errorString, ioException) ;
			throw new BookingError(Constants.ERR_CODE_PROCESANDO_DIA_SEMANA, errorString, ioException) ;
        }
        finally
        {
        	this.cerrarFlujo(reader) ;
        }

        // Guardamos los días de la semana en la base de datos
        if (!diasSemana.isEmpty())
        {
            this.diaSemanaRepository.saveAllAndFlush(diasSemana) ;
        }
	}
	
	/**
	 * @param reader reader
	 * @throws BookingError excepción mientras se cerraba el reader
	 */
	private void cerrarFlujo(BufferedReader reader) throws BookingError
	{
		if (reader != null)
		{
		    try
		    {
		    	// Cierre del reader
				reader.close() ;
			}
		    catch (IOException ioException)
		    {
				String errorString = "IOException mientras se cerraba el reader" ;
				
				log.error(errorString, ioException) ;
				throw new BookingError(Constants.ERR_CODE_CIERRE_READER, errorString, ioException) ;
			}	
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
		this.cargarPropiedad(Constants.TABLA_CONST_RESERVAS_TEMPORALES, this.reservasTemporales);
		this.cargarPropiedad(Constants.TABLA_CONST_MAX_VISTA_CAL_DIAS, this.maxDiasCalendario);
	}

	/**
	 * @param key clave
	 * @param value valor
	 */
	private void cargarPropiedad(String key, String value)
	{
		// Verificamos si tiene algún valor
		Optional<Constantes> property = this.constantesRepository.findById(key);

		// Si está vacío, lo seteamos con el valor del YAML
		if (property.isEmpty())
		{
			Constantes constante = new Constantes();

			constante.setClave(key);
			constante.setValor(value);

			// Almacenamos la constante en BBDD
			this.constantesRepository.save(constante);
		}
	}
}
