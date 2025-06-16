package es.iesjandula.reaktor.booking_server.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Clase de configuración para habilitar CORS (Cross-Origin Resource Sharing) en
 * la aplicación.
 * <p>
 * Esta clase permite que direcciones IP remotas puedan comunicarse con el
 * backend. Los orígenes permitidos se definen en el archivo de propiedades
 * mediante la variable {@code reaktor.urlCors}.
 * </p>
 * 
 * <p>
 * Se permiten los métodos HTTP: GET, POST, PUT y DELETE, así como cualquier
 * cabecera.
 * </p>
 * 
 * @author Luis David Castillo
 * @author Miguel Ríos
 * @author Enrique Contreras
 */
@Configuration
@EnableWebMvc
public class CORSConfig implements WebMvcConfigurer
{

	/** Lista de URLs permitidas para realizar peticiones al backend. */
	@Value("${reaktor.urlCors}")
	private String[] urlCors;

	/**
	 * Configura los mapeos de CORS para permitir solicitudes cruzadas desde los
	 * orígenes permitidos.
	 *
	 * @param registry Objeto utilizado para registrar las configuraciones de CORS.
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry)
	{
		registry.addMapping("/**").allowedOrigins(urlCors).allowedMethods("GET", "POST", "PUT", "DELETE")
				.allowedHeaders("*");
	}
}
