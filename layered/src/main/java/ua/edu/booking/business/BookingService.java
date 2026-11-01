package ua.edu.booking.business;

import ua.edu.booking.data.BookingDao;
import ua.edu.booking.data.EmailSender;
import ua.edu.booking.data.RoomDao;
import ua.edu.booking.model.Booking;
import ua.edu.booking.model.Room;

import java.time.LocalDateTime;
import java.util.List;

/** Усі бізнес-правила в одному сервісі. Залежності створює сам — тестувати можна лише з файлом. */
public class BookingService {

    private static final int MAX_MINUTES = 180;

    private final BookingDao bookingDao = new BookingDao();
    private final RoomDao roomDao = new RoomDao();
    private final EmailSender emailSender = new EmailSender();

    public Booking book(String roomNumber, LocalDateTime start, int minutes, String who,
                        int attendees, boolean needsProjector) {
        Room room = roomDao.findByNumber(roomNumber)
                .orElseThrow(() -> new BookingException("аудиторії " + roomNumber + " не існує"));
        if (start.isBefore(LocalDateTime.now())) {
            throw new BookingException("не можна бронювати в минулому");
        }
        if (minutes <= 0 || minutes > MAX_MINUTES) {
            throw new BookingException("тривалість має бути від 1 до " + MAX_MINUTES + " хв");
        }
        if (attendees <= 0) {
            throw new BookingException("кількість осіб має бути додатною");
        }
        if (attendees > room.capacity()) {
            throw new BookingException("аудиторія " + room.number() + " вміщує лише " + room.capacity() + " осіб");
        }
        if (needsProjector && !room.projector()) {
            throw new BookingException("в аудиторії " + room.number() + " немає проєктора");
        }
        LocalDateTime end = start.plusMinutes(minutes);
        for (Booking b : bookingDao.findByRoom(roomNumber)) {
            if (start.isBefore(b.end()) && b.start().isBefore(end)) {
                throw new BookingException("аудиторія " + roomNumber + " вже зайнята в цей час");
            }
        }

        Booking booking = new Booking(bookingDao.nextId(), roomNumber, start, end, who, attendees, needsProjector);
        bookingDao.save(booking);
        emailSender.send(who, "Бронювання #" + booking.id() + ": ауд. " + roomNumber + ", "
                + start + "–" + end.toLocalTime());
        return booking;
    }

    public Booking cancel(int id, String who) {
        Booking booking = bookingDao.findById(id)
                .orElseThrow(() -> new BookingException("бронювання #" + id + " не знайдено"));
        if (!booking.who().equals(who)) {
            throw new BookingException("скасувати може лише автор бронювання");
        }
        bookingDao.delete(id);
        emailSender.send(who, "Бронювання #" + id + " скасовано");
        return booking;
    }

    public List<Booking> list(String roomOrNull) {
        return roomOrNull == null ? bookingDao.findAll() : bookingDao.findByRoom(roomOrNull);
    }
}
