package es.iesjandula.reserva_carritos.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import es.iesjandula.reserva_carritos.models.reservas_puntuales.Booking;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Classroom;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Teacher;
import es.iesjandula.reserva_carritos.models.reservas_puntuales.Trolley;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

/**
 * @author Manuel y Miguel
 */

@Log4j2
public class Utils
{
	public boolean comprobarHora(double hora)
	{
		if (hora == 8.15 || hora == 9.15 || hora == 10.15 || hora == 11.15 || hora == 11.45 || hora == 12.45
				|| hora == 13.45)
		{
			return true;
		} else
		{
			return false;
		}
	}

	public String validateBooking(HttpSession session, List<Booking> dateBookingList, List<Booking> bookingList,
			Date currentDate, String message, String date) throws ParseException
	{
		List<Classroom> classroom;
		List<Trolley> trolley;
		if (date != null)
		{
			classroom = (List<Classroom>) session.getAttribute("classroomList");
			trolley = (List<Trolley>) session.getAttribute("trolleyList");
			bookingList.addAll(classroom);
			bookingList.addAll(trolley);
			for (Booking booking : classroom)
			{
				if (!booking.getDate().isEmpty() && booking.getDate().contains(date))
				{
					SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
					Date dateParsed = formato.parse(booking.getDate());
					dateBookingList.add(booking);
				}
			}
			for (Booking booking : trolley)
			{
				if (!booking.getDate().isEmpty() && booking.getDate().contains(date))
				{
					SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
					Date dateParsed = formato.parse(booking.getDate());
					dateBookingList.add(booking);
				}
			}
		} else
		{
			if (session.getAttribute("classroomList") != null && session.getAttribute("trolleyList") != null)
			{
				classroom = (List<Classroom>) session.getAttribute("classroomList");
				trolley = (List<Trolley>) session.getAttribute("trolleyList");
				bookingList.addAll(classroom);
				bookingList.addAll(trolley);
				for (Booking booking : classroom)
				{
					if (!booking.getDate().isEmpty())
					{
						SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
						Date dateParsed = formato.parse(booking.getDate());
						dateBookingList.add(booking);
					}
				}
				for (Booking booking : trolley)
				{
					if (!booking.getDate().isEmpty())
					{
						SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
						Date dateParsed = formato.parse(booking.getDate());
						dateBookingList.add(booking);
					}
				}

			} else if (session.getAttribute("classroomList") != null)
			{
				classroom = (List<Classroom>) session.getAttribute("classroomList");
				bookingList.addAll(classroom);
				for (Booking booking : classroom)
				{
					if (!booking.getDate().isEmpty())
					{
						SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
						Date dateParsed = formato.parse(booking.getDate());
						dateBookingList.add(booking);
					}
				}
			} else if (session.getAttribute("trolleyList") != null)
			{
				trolley = (List<Trolley>) session.getAttribute("trolleyList");
				bookingList.addAll(trolley);
				for (Booking booking : trolley)
				{
					if (!booking.getDate().isEmpty())
					{
						SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
						Date dateParsed = formato.parse(booking.getDate());
						dateBookingList.add(booking);
					}
				}
			} else
			{
				message = "No hay ninguna reserva realizada";
				log.error(message);
			}
		}
		session.setAttribute("bookingList", bookingList);
		return message;
	}

	/**
	 * check if you can book a trolley
	 * 
	 * @param marca
	 * @param piso
	 * @param nombre
	 * @param apellido
	 * @param dni
	 * @param dateCompleted
	 * @param foundTrolley
	 * @param resultado
	 * @param correctHour
	 * @param correctDay
	 * @param trolleyList
	 * @return
	 */
	public String ValidateBookingTrolley(String marca, String piso, String nombre, String apellido, String dni,
			String dateCompleted, boolean foundTrolley, boolean correctTrolleyNumber, String resultado,
			boolean correctHour, boolean correctDay, String deviceType, Integer trolleyNumber,
			List<Trolley> trolleyList, HttpSession session)
	{
		if (!foundTrolley && correctDay && correctHour && correctTrolleyNumber)
		{
			Trolley trolley = new Trolley();
			Teacher teacher = new Teacher();
			trolley.setDate(dateCompleted);
			teacher.setDni(dni);
			teacher.setName(nombre);
			teacher.setLastname(apellido);
			trolley.setFloor(piso);
			trolley.setBrand(marca);
			trolley.setTeacher(teacher);
			trolley.setDeviceType(deviceType);
			trolley.setTrolleyNumber(trolleyNumber);
			resultado = "Reserva realizada correctamente";
			trolleyList.add(trolley);
			// save the list with the changes in session
			session.setAttribute("trolleyList", trolleyList);
		} else if (!correctHour)
		{
			resultado = "La hora no es correcta";
		} else if (!foundTrolley && !correctDay)
		{
			resultado = "No se puede hacer una reserva el fin de semana";
		} else if (foundTrolley && correctDay)
		{
			resultado = "El carrito ya esta reservado";
		} else if (!correctTrolleyNumber)
		{
			resultado = "El carrito no existe";
		}
		return resultado;
	}

	/**
	 * check if you can book a classroom
	 * 
	 * @param aula
	 * @param nombre
	 * @param apellido
	 * @param dni
	 * @param session
	 * @param dateCompleted
	 * @param foundClassroom
	 * @param correctDay
	 * @param resultado
	 * @param correctHour
	 * @param classroomList
	 * @return
	 */
	public String ValidateClassroomBooking(String aula, String nombre, String apellido, String dni, HttpSession session,
			String dateCompleted, boolean foundClassroom, boolean correctDay, String resultado, boolean correctHour,
			List<Classroom> classroomList)
	{
		if (!foundClassroom && correctDay && correctHour)
		{
			Classroom classroomObject = new Classroom();
			Teacher teacher = new Teacher();
			classroomObject.setAula(aula);
			classroomObject.setDate(dateCompleted);
			teacher.setDni(dni);
			teacher.setName(nombre);
			teacher.setLastname(apellido);
			classroomObject.setTeacher(teacher);
			classroomList.add(classroomObject);
			// save the list with the changes in session
			session.setAttribute("classroomList", classroomList);
			resultado = "Reserva realizada correctamente";
		} else if (!correctHour)
		{
			resultado = "La hora no es correcta";
		} else if (!foundClassroom && !correctDay)
		{
			resultado = "No se puede hacer una reserva el fin de semana";
		} else if (foundClassroom && correctDay)
		{
			resultado = "La clase ya esta reservada";
		}
		return resultado;
	}
}