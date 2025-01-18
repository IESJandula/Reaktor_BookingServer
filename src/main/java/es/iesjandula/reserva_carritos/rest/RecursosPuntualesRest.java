package es.iesjandula.reserva_carritos.rest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.iesjandula.reserva_carritos.exception.BookingError;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Booking;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Classroom;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Holidays;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Teacher;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Trolley;
import es.iesjandula.reserva_carritos.utils.Utils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

/**
 * @author Manuel y Miguel
 */

@RestController
@RequestMapping(value = "/bookings", produces = "application/json")
@Log4j2
public class RecursosPuntualesRest
{

	@RequestMapping(method = RequestMethod.POST, value = "/configDates")
	public ResponseEntity<?> configDates(@RequestParam(value = "fechaInicio", required = true) String inicio,
			@RequestParam(value = "fechaFinal", required = true) String fin, HttpSession session)
	{
		try
		{
			List<String> dateConfig = new ArrayList<String>();
			if (session.getAttribute("dataConfig") != null)
			{
				dateConfig = (List<String>) session.getAttribute("dataConfig");
				dateConfig.removeAll(dateConfig);
				dateConfig.add(inicio);
				dateConfig.add(fin);
				session.setAttribute("dataConfig", dateConfig);
			} else
			{
				dateConfig.add(inicio);
				dateConfig.add(fin);
				session.setAttribute("dataConfig", dateConfig);
			}
			String message = "Fechas asignadas correctamente";
			log.info(message);
			return ResponseEntity.ok().body(message);
		} catch (Exception exception)
		{
			String message = "Error generico";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			// Al no controlar que fallo puede llegar a tener el servidor devolvemos un 500,
			// informando del fallo real que ha ocurrido.
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/configDates")
	public ResponseEntity<?> configDates(HttpSession session)
	{
		try
		{
			List<String> dateConfig = new ArrayList<String>();

			if (session.getAttribute("dataConfig") != null)
			{
				dateConfig = (List<String>) session.getAttribute("dataConfig");
			} else
			{
				String message = "Todavía no existe una configuración de Fecha de Inicio y Fin";
				log.error(message);
				BookingError bookingError = new BookingError(0, message);
				return ResponseEntity.status(404).body(message);
			}
			log.info(dateConfig);
			return ResponseEntity.ok().body(dateConfig);
		} catch (Exception exception)
		{
			String message = "Server Error";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		}
	}

	/**
	 * endpoint para cargar las aulas
	 * 
	 * @param csvFile
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/classroom", consumes = "multipart/form-data")
	public ResponseEntity<?> postClassroom(@RequestPart(value = "csvFile", required = true) MultipartFile csvFile,
			HttpSession session)
	{
		Scanner scanner = null;
		try
		{
			String csvContent = new String(csvFile.getBytes());
			// Se crea un scanner para leer el contenido del csv
			scanner = new Scanner(csvContent);
			// Nos saltamos la primera linera que es la cabecera
			scanner.nextLine();
			// Creamos una lista de Aulas para almacenar el contenido del csv
			List<Classroom> classroomsList = new ArrayList<Classroom>();
			// Creamos un bucle para leer hasta terminar el csv
			while (scanner.hasNextLine())
			{
				// Leemos la primera linea del fichero
				String line = scanner.nextLine();
				// Almacenamos en una variable token la limitación de cada columna: CSV =
				// Separación de Comas
				String[] token = line.split(",");

				// Cogemos la columna 0,1,2 que son las que tienen el objeto Profesor
				String name = token[0];
				String lastname = token[1];
				String dni = token[2];
				// Creamos el objeto directamente inicializandolo con las variables que
				// contienen los datos del csv
				Teacher teacher = new Teacher(name, lastname, dni);
				// Cogemos la columna 3,4 que son las que contienen las variables del objecto
				// Aula
				String aula = token[3];
				String date = token[4];
				// Creamos el objeto directamente inicializandolo con las variables que
				// contienen los datos del csv
				Classroom classroom = new Classroom(teacher, date, aula);
				// Añadimos el objeto a la lista
				classroomsList.add(classroom);
				// Y guardamos la lista en session
				session.setAttribute("classroomList", classroomsList);
			}
			// Creamos una variable para mostrar que todo a ido correctamente
			String message = "Data loading was successful";
			log.info(message);
			// Devolvemos en un 200, el mensaje de la variable
			return ResponseEntity.ok().body(message);

		} catch (IOException ioException)
		{
			// Si hay algún fallo al leer el csv nos saldrá éste error de lectura
			// entrada/salida
			String message = "I/O ERROR";
			log.error(message, ioException);
			BookingError exerciseError = new BookingError(1, message, ioException);
			// Devolvemos en forma de mapa el objeto propio con la información del fallo
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		} catch (Exception exception)
		{
			// Si hay algun fallo que no se controle devolvemos un error generico
			String message = "Error generico";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			// Al no controlar que fallo puede llegar a tener el servidor devolvemos un 500,
			// informando del fallo real que ha ocurrido.
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		} finally
		{
			if (scanner != null)
			{
				// Cerramos el escaner y liberamos la memoria.
				scanner.close();
			}
		}
	}

	/**
	 * metodo para obtener las aulas
	 * 
	 * @param session
	 * @param classroom
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/classroom")
	public ResponseEntity<?> getBookingsByClassroom(HttpSession session,
			@RequestParam(value = "classroom", required = false) String aula)
	{
		try
		{
			List<Classroom> classroomList = null;
			List<Classroom> classroomListSend = new ArrayList<Classroom>();
			// comprobamos que la lista guardada en session no este vacia
			if (session.getAttribute("classroomList") != null)
			{
				// obtenemos la lista guardada en session
				classroomList = (List<Classroom>) session.getAttribute("classroomList");
				for (Classroom classroomObject : classroomList)
				{
					// comprobamos si nos han mandado alguna aula
					if (aula != null)
					{
						// buscamos los objetos con la aula que nos han mandado y los guardamos en una
						// lista
						if (aula.equals(classroomObject.getAula()))
						{
							Classroom classroomTosend = classroomObject;
							classroomListSend.add(classroomTosend);
						}
					}
					// si no pasan ningun aula mandamos todas las aulas
					else
					{
						Classroom classroomTosend = classroomObject;
						classroomListSend.add(classroomTosend);
					}
				}
			} else
			{
				String message = "No se ha realizado ninguna reserva de aula todavía";
				log.error(message);
				return ResponseEntity.status(400).body(message);
			}

			return ResponseEntity.ok().body(classroomListSend);
		} catch (Exception exception)
		{
			String message = "Server Error";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		}
	}

	/**
	 * endpoint para cargar los carritos
	 * 
	 * @param csvFile
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/trolley", consumes = "multipart/form-data")
	public ResponseEntity<?> postTrolley(@RequestPart(value = "csvFile", required = true) MultipartFile csvFile,
			HttpSession session)
	{
		Scanner scanner = null;
		try
		{
			String csvContent = new String(csvFile.getBytes());
			// Se crea un scanner para leer el contenido del csv
			scanner = new Scanner(csvContent);
			// Nos saltamos la primera linera que es la cabecera
			scanner.nextLine();
			// Creamos una lista de carritos para almacenar los objetos carritos
			List<Trolley> trolleyList = new ArrayList<Trolley>();

			// Creamos un bucle para leer hasta terminar el csv
			while (scanner.hasNextLine())
			{
				// Leemos la primera linea del fichero
				String line = scanner.nextLine();
				// Almacenamos en una variable token la limitación de cada columna: CSV =
				// Separación de Comas
				String[] token = line.split(",");

				// Cogemos la columna 0,1,2 que son las que tienen el objeto Profesor
				String name = token[0];
				String lastname = token[1];
				String dni = token[2];
				// Creamos el objeto directamente inicializandolo con las variables que
				// contienen los datos del csv
				Teacher teacher = new Teacher(name, lastname, dni);

				// Cogemos la columna 3,4,5 que son las que contienen las variables del objecto
				// Carrito
				String brand = token[3];
				String floor = token[4];
				String date = token[5];
				String deviceType = token[6];
				String number = token[7];
				// Creamos el objeto directamente inicializandolo con las variables que
				// contienen los datos del csv
				Trolley trolley = new Trolley(teacher, date, brand, floor, deviceType, Integer.parseInt(number));
				// Añadimos el objeto a la lista
				trolleyList.add(trolley);
				// Subimos la lista de carritos en session
				session.setAttribute("trolleyList", trolleyList);
			}
			// Creamos una variable para mostrar que todo a ido correctamente
			String message = "Data loading was successful";
			log.info(trolleyList);
			// Devolvemos en un 200 el mensaje de la variable
			return ResponseEntity.ok().body(message);

		} catch (IOException ioException)
		{
			// Si hay algún fallo al leer el csv nos saldrá éste error de lectura
			// entrada/salida
			String message = "I/O ERROR";
			log.error(message, ioException);
			BookingError exerciseError = new BookingError(1, message, ioException);
			// Devolvemos en forma de mapa el objeto propio con la información del fallo
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		} catch (Exception exception)
		{
			// Si hay algun fallo que no se controle devolvemos un error generico
			String message = "Error generico";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			// Al no controlar que fallo puede llegar a tener el servidor devolvemos un 500,
			// informando del fallo real que ha ocurrido.
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		} finally
		{
			if (scanner != null)
			{
				// Cerramos el escaner y liberamos la memoria.
				scanner.close();
			}
		}
	}

	/**
	 * endpoint to get bookings after now
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/allbookings")
	public ResponseEntity<?> getAllBookings(HttpSession session,
			@RequestParam(value = "fecha", required = false) String date)
	{
		try
		{
			// Create a Utils object for utility functions
			Utils utils = new Utils();
			// List to store bookings after the current date
			List<Booking> dateBookingList = new ArrayList<Booking>();
			// List to store all bookings
			List<Booking> bookingList = new ArrayList<Booking>();
			// Get the current date
			Date currentDate = new Date();
			// Initialize an empty message
			String message = "";
			// Call a method from Utils class to validate bookings
			message = utils.validateBooking(session, dateBookingList, bookingList, currentDate, message, date);

			// If no validation error, return the list of bookings after the current date
			if (message.isEmpty())
			{
				return ResponseEntity.ok().body(dateBookingList);
			}
			// If there's a validation error, return an error response with the message
			else
			{
				return ResponseEntity.status(400).body(message);
			}
		} catch (ParseException parseException)
		{
			// Error message for date parsing exception
			String message = "Error parsing date";
			log.error(message, parseException);
			BookingError exerciseError = new BookingError(1, message, parseException);
			// Return an error response with details about the parsing exception
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		} catch (Exception exception)
		{
			// Error message for general server error
			String message = "Server Error";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			// Return an error response with details about the general exception
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/holidays", consumes = "multipart/form-data")
	public ResponseEntity<?> postHolidays(@RequestPart(value = "csvFile", required = true) MultipartFile csvFile,
			HttpSession session)
	{
		Scanner scanner = null;
		try
		{
			String csvContent = new String(csvFile.getBytes());
			// Se crea un scanner para leer el contenido del csv
			scanner = new Scanner(csvContent);
			// Nos saltamos la primera linera que es la cabecera
			scanner.nextLine();
			// Creamos una lista de carritos para almacenar los objetos carritos
			List<Holidays> holidaysList = new ArrayList<Holidays>();

			// Creamos un bucle para leer hasta terminar el csv
			while (scanner.hasNextLine())
			{
				// Leemos la primera linea del fichero
				String line = scanner.nextLine();
				// Almacenamos en una variable token la limitación de cada columna: CSV =
				// Separación de Comas
				String[] token = line.split(",");
				// Cogemos la columna 0,1,2 que son las que tienen el objeto Profesor
				String date = token[0];
				String info = token[1];
				// Creamos el objeto directamente inicializandolo con las variables que
				// contienen los datos del csv
				Holidays holiday = new Holidays(date, info);
				// Añadimos el objeto a la lista
				holidaysList.add(holiday);
				// Subimos la lista de carritos en session
				session.setAttribute("holidaysList", holidaysList);
			}
			// Creamos una variable para mostrar que todo a ido correctamente
			String message = "Data loading was successful";
			log.info(message);
			// Devolvemos en un 200 el mensaje de la variable
			return ResponseEntity.ok().body(message);

		} catch (IOException ioException)
		{
			// Si hay algún fallo al leer el csv nos saldrá éste error de lectura
			// entrada/salida
			String message = "I/O ERROR";
			log.error(message, ioException);
			BookingError exerciseError = new BookingError(1, message, ioException);
			// Devolvemos en forma de mapa el objeto propio con la información del fallo
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		} catch (Exception exception)
		{
			// Si hay algun fallo que no se controle devolvemos un error generico
			String message = "Error generico";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			// Al no controlar que fallo puede llegar a tener el servidor devolvemos un 500,
			// informando del fallo real que ha ocurrido.
			return ResponseEntity.status(500).body(exerciseError.getMapError());
		} finally
		{
			if (scanner != null)
			{
				// Cerramos el escaner y liberamos la memoria.
				scanner.close();
			}
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/holidays")
	public ResponseEntity<?> getHolidays(HttpSession session)
	{
		try
		{
			List<Holidays> holidaysList = new ArrayList<Holidays>();

			if (session.getAttribute("holidaysList") != null)
			{
				holidaysList = (List<Holidays>) session.getAttribute("holidaysList");
			} else
			{
				String message = "No existe una lista de Festivos todavía";
				log.error(message);
				BookingError bookingError = new BookingError(0, message);
				return ResponseEntity.status(404).body(bookingError);
			}

			return ResponseEntity.ok().body(holidaysList);
		} catch (Exception exception)
		{
			String message = "Server Error";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		}
	}

	/**
	 * endpoint para obtener los carritos de tablets
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/trolleytablet")
	public ResponseEntity<?> getTrolleyTablet(HttpSession session)
	{
		try
		{
			// obtenemos la lista de carritos guardada en session
			List<Trolley> trolleyListSession = (List<Trolley>) session.getAttribute("trolleyList");
			List<Trolley> trolleyTabletList = new ArrayList<Trolley>();
			for (Trolley trolleyCsv : trolleyListSession)
			{
				// recorremos la lista de carritos u guardamos las tablets
				if (trolleyCsv.getDeviceType().equalsIgnoreCase("Tablet"))
				{
					Trolley trolley = trolleyCsv;
					trolleyTabletList.add(trolley);
				}
			}
			return ResponseEntity.ok().body(trolleyTabletList);
		} catch (Exception exception)
		{
			String message = "Error finding tablets";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		}
	}

	/**
	 * endpoint para obntener los carritos de pc
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/trolleyportatil")
	public ResponseEntity<?> getTrolleyPc(HttpSession session)
	{
		try
		{
			// obtenemos la lista de carritos guardada en session
			List<Trolley> trolleyListSession = (List<Trolley>) session.getAttribute("trolleyList");
			List<Trolley> trolleyPcList = new ArrayList<Trolley>();
			// recorremos la lista de carritos u guardamos los pcs
			for (Trolley trolleyCsv : trolleyListSession)
			{
				if (trolleyCsv.getDeviceType().equalsIgnoreCase("Portatil"))
				{
					Trolley trolley = trolleyCsv;
					trolleyPcList.add(trolley);
				}
			}
			return ResponseEntity.ok().body(trolleyPcList);
		} catch (Exception exception)
		{
			String message = "Error finding pcs";
			log.error(message, exception);
			BookingError exerciseError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(exerciseError.getMapError());
		}
	}

	/**
	 * endpoint para cancelar una reserva de carrito
	 * 
	 * @param date
	 * @param hora
	 * @param marca
	 * @param piso
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/cancelTrolley")
	public ResponseEntity<?> CancelTrolley(@RequestParam(value = "fecha", required = true) String date,
			@RequestParam(value = "hora", required = true) String hora,
			@RequestParam(value = "marca", required = true) String marca,
			@RequestParam(value = "piso", required = true) String piso,
			@RequestParam(value = "deviceType", required = true) String deviceType,
			@RequestParam(value = "trolleyNumber", required = true) Integer trolleyNumber, HttpSession session)
	{
		try
		{
			// formato de parseo de la fecha
			SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			String dateCompleted = date + " " + hora;
			// parseamos la fecha
			Date dateParsed = formato.parse(dateCompleted);
			// obtenemos la lista de carritos guardada en session
			List<Trolley> trolleyList = (List<Trolley>) session.getAttribute("trolleyList");
			// resultado por defecto
			String resultado = "Esa reserva no existe";
			int i = 0;
			boolean foundReservation = false;
			while (i < trolleyList.size() && !foundReservation)
			{
				// comprueba si la reserva esta en la lista
				Date trolleydate = formato.parse(trolleyList.get(i).getDate());
				if (trolleydate.getTime() == dateParsed.getTime() && trolleyList.get(i).getFloor().equals(piso)
						&& trolleyList.get(i).getBrand().equals(marca)
						&& trolleyList.get(i).getDeviceType().equalsIgnoreCase(deviceType)
						&& trolleyList.get(i).getTrolleyNumber().equals(trolleyNumber))
				{
					// eliminamos la reserva y cambiamos foundReservation a true para que salga del
					// while
					trolleyList.remove(i);
					resultado = "Reserva cancelada correctamente";
					foundReservation = true;
				}
				i++;
			}
			// guardamos la lista con los cambios en session
			session.setAttribute("trolleyList", trolleyList);

			return ResponseEntity.ok().body(resultado);
		} catch (Exception exception)
		{
			String message = "Error with the trolley cancel";
			log.error(message, exception);
			BookingError bookingError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(bookingError.getMapError());
		}
	}

	/**
	 * endpoint para cancelar una reserva de aula
	 * 
	 * @param date
	 * @param hora
	 * @param aula
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/cancelClassroom")
	public ResponseEntity<?> cancelClassroom(@RequestParam(value = "fecha", required = true) String date,
			@RequestParam(value = "hora", required = true) String hora,
			@RequestParam(value = "aula", required = true) String aula, HttpSession session)
	{
		try
		{
			// formato de parseo de la fecha
			SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			String dateCompleted = date + " " + hora;
			// parseamos la fecha
			Date dateParsed = formato.parse(dateCompleted);
			// obtenemos la lista de aulas guardada en session
			List<Classroom> classroomList = (List<Classroom>) session.getAttribute("classroomList");
			// resultado por defecto
			String resultado = "Esa reserva no existe";
			int i = 0;
			boolean foundReservation = false;
			while (i < classroomList.size() && !foundReservation)
			{
				// comprueba si la reserva esta en la lista
				Date classroomdate = formato.parse(classroomList.get(i).getDate());
				if (classroomdate.getTime() == dateParsed.getTime() && classroomList.get(i).getAula().equals(aula))
				{
					// eliminamos la reserva y cambiamos foundReservation a true para que salga del
					// while
					classroomList.remove(i);
					resultado = "Reserva cancelada correctamente";
					foundReservation = true;
				}
				i++;
			}
			// guardamos la lista con los cambios en session
			session.setAttribute("classroomList", classroomList);

			return ResponseEntity.ok().body(resultado);
		} catch (Exception exception)
		{
			String message = "Server error";
			log.error(message, exception);
			BookingError bookingError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(bookingError.getMapError());
		}
	}

	/**
	 * endpoint para realizar una reserva de aula
	 * 
	 * @param date
	 * @param hora
	 * @param aula
	 * @param nombre
	 * @param apellido
	 * @param dni
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/bookingClassroom")
	public ResponseEntity<?> BookingClassroom(@RequestParam(value = "fecha", required = true) String date,
			@RequestParam(value = "hora", required = true) String hora,
			@RequestParam(value = "aula", required = true) String aula,
			@RequestParam(value = "nombre", required = true) String nombre,
			@RequestParam(value = "apellido", required = true) String apellido,
			@RequestParam(value = "dni", required = true) String dni, HttpSession session)
	{
		try
		{
			// formato de parseo de la fecha
			SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			SimpleDateFormat formatoDataConfig = new SimpleDateFormat("yyyy/MM/dd");
			String dateCompleted = date + " " + hora;
			List<String> dataConfig = (List<String>) session.getAttribute("dataConfig");
			String inicio = dataConfig.get(0);
			String fin = dataConfig.get(1);
			Date dateInit = formatoDataConfig.parse(inicio);
			Date dateLast = formatoDataConfig.parse(fin);
			// parseo de la fecha
			Date dateParsed = formato.parse(dateCompleted);
			// String to save the result
			String resultado = "";

			if (dateParsed.before(dateLast) && dateParsed.after(dateInit))
			{
				// booleans para comprobar si existe el aula y si la hora y el dia introducidos
				// son correctos
				boolean foundClassroom = false;
				boolean correctDay = true;
				boolean correctHour = false;

				Utils utils = new Utils();
				// obtenemos la lista guardada en session
				List<Classroom> classroomList = (List<Classroom>) session.getAttribute("classroomList");
				hora = hora.replace(":", ".");
				// comprobamos que la hora que se ha mandado sea correcta
				correctHour = utils.comprobarHora(Double.valueOf(hora));
				int i = 0;
				// recorremos la lista para comprobar si la reserva existe en esta
				while (i < classroomList.size() && !foundClassroom)
				{
					Date classroomdate = formato.parse(classroomList.get(i).getDate());
					// obtenemos el dia de la semana que ha introducido el usuario
					int diaSemana = dateParsed.getDay();
					if (correctHour)
					{
						// comprobamos que el dia introducido no sea sabado ni domingo
						if (diaSemana != 6 && diaSemana != 0)
						{
							// comprobamos si la reserva mandada por el usuario existe
							if (classroomdate.getTime() == dateParsed.getTime()
									&& classroomList.get(i).getAula().equals(aula))
							{
								foundClassroom = true;
							}
						} else
						{
							correctDay = false;
						}
					}
					i++;
				}
				// metodo para validar la reserva de una aula
				resultado = utils.ValidateClassroomBooking(
						aula, nombre, apellido, dni, session, dateCompleted, foundClassroom, correctDay, resultado,
						correctHour, classroomList
				);
			} else
			{
				resultado = "No se puede hacer una reserva cuando no hay instituto";
				log.info(resultado);
			}
			log.info(resultado);
			return ResponseEntity.ok().body(resultado);
		} catch (Exception exception)
		{
			String message = "Error booking a classroom";
			log.error(message, exception);
			BookingError bookingError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(bookingError.getMapError());
		}
	}

	/**
	 * endpoint para realizar una reserva de carritos
	 * 
	 * @param date
	 * @param hora
	 * @param marca
	 * @param piso
	 * @param nombre
	 * @param apellido
	 * @param dni
	 * @param session
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/bookingTrolley")
	public ResponseEntity<?> BookingTrolley(@RequestParam(value = "fecha", required = true) String date,
			@RequestParam(value = "hora", required = true) String hora,
			@RequestParam(value = "marca", required = true) String marca,
			@RequestParam(value = "deviceType", required = true) String deviceType,
			@RequestParam(value = "trolleyNumber", required = true) Integer trolleyNumber,
			@RequestParam(value = "piso", required = true) String piso,
			@RequestParam(value = "nombre", required = true) String nombre,
			@RequestParam(value = "apellido", required = true) String apellido,
			@RequestParam(value = "dni", required = true) String dni, HttpSession session)
	{
		try
		{
			// formato de parseo de la fecha
			SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			String dateCompleted = date + " " + hora;
			// parseo de la fecha
			Date dateParsed = formato.parse(dateCompleted);
			// booleans para comprobar si existe el carrito y si la hora y el dia
			// introducidos son correctos
			boolean foundTrolley = false;
			boolean correctHour = false;
			boolean correctDay = true;
			boolean correctTrolleyNumber = false;
			// string para guarda el resultado
			String result = "";
			Utils utils = new Utils();
			// obtenemos la lista guardada en session
			List<Trolley> trolleyList = (List<Trolley>) session.getAttribute("trolleyList");
			// reemplazamos los : por . de la hora que nos pasa el usuario para pasarla a
			// metodo de comprobar hora
			hora = hora.replace(":", ".");
			// comprobamos que la hora que se ha mandado sea correcta
			correctHour = utils.comprobarHora(Double.valueOf(hora));
			int i = 0;
			// recorremos la lista para comprobar si la reserva existe en esta
			while (i < trolleyList.size() && !foundTrolley)
			{
				Date trolleydate = formato.parse(trolleyList.get(i).getDate());
				// obtenemos el dia de la semana que ha introducido el usuario
				int diaSemana = dateParsed.getDay();
				if (correctHour)
				{
					// comprobamos que el dia introducido no sea sabado ni domingo
					if (diaSemana != 6 && diaSemana != 0)
					{
						if (deviceType.equalsIgnoreCase("portatil"))
						{
							if (trolleyNumber <= 2 && trolleyNumber > 0)
							{
								correctTrolleyNumber = true;
								// comprobamos si la reserva mandada por el usuario existe
								if (trolleydate.getTime() == dateParsed.getTime()
										&& trolleyList.get(i).getFloor().equals(piso)
										&& trolleyList.get(i).getBrand().equals(marca)
										&& trolleyList.get(i).getDeviceType().equalsIgnoreCase(deviceType)
										&& trolleyList.get(i).getTrolleyNumber().equals(trolleyNumber))
								{
									foundTrolley = true;
								}
							}
						} else if (deviceType.equalsIgnoreCase("tablet"))
						{
							if (trolleyNumber == 1)
							{
								correctTrolleyNumber = true;
								// comprobamos si la reserva mandada por el usuario existe
								if (trolleydate.getTime() == dateParsed.getTime()
										&& trolleyList.get(i).getFloor().equals(piso)
										&& trolleyList.get(i).getBrand().equals(marca)
										&& trolleyList.get(i).getDeviceType().equalsIgnoreCase(deviceType)
										&& trolleyList.get(i).getTrolleyNumber().equals(trolleyNumber))
								{
									foundTrolley = true;
								}
							}
						}
					} else
					{
						correctDay = false;
					}
				}
				i++;
			}
			// metodo para validar una reserva de carritos
			result = utils.ValidateBookingTrolley(
					marca, piso, nombre, apellido, dni, dateCompleted, foundTrolley, correctTrolleyNumber, result,
					correctHour, correctDay, deviceType, trolleyNumber, trolleyList, session
			);
			log.info(result);
			return ResponseEntity.ok().body(result);
		} catch (Exception exception)
		{
			String message = "Error booking a trolley";
			log.error(message, exception);
			BookingError bookingError = new BookingError(1, message, exception);
			return ResponseEntity.status(400).body(bookingError.getMapError());
		}
	}
}