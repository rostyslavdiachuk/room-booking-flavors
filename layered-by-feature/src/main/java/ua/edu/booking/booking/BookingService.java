package ua.edu.booking.booking;

import ua.edu.booking.notification.Mailer;
import ua.edu.booking.room.Room;
import ua.edu.booking.room.RoomCatalog;

import java.time.LocalDateTime;
import java.util.List;

/** Бізнес-шар фічі «бронювання». Про аудиторії знає лише через публічний RoomCatalog. */
class BookingService {

    private static final int MAX_MINUTES = 180;

    private final BookingRepository repository;
    private final RoomCatalog rooms;
    private final Mailer mailer;

    BookingService(BookingRepository repository, RoomCatalog rooms, Mailer mailer) {
        this.repository = repository;
        this.rooms = rooms;
        this.mailer = mailer;
    }

    Booking book(String roomNumber, LocalDateTime start, int minutes, String who, int attendees, boolean needsProjector) {
        Room room = rooms.findByNumber(roomNumber)
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
        for (Booking b : repository.findByRoom(roomNumber)) {
            if (start.isBefore(b.end()) && b.start().isBefore(end)) {
                throw new BookingException("аудиторія " + roomNumber + " вже зайнята в цей час");
            }
        }
        Booking booking = new Booking(repository.nextId(), roomNumber, start, end, who, attendees, needsProjector);
        repository.save(booking);
        mailer.send(who, "Бронювання #" + booking.id() + ": ауд. " + roomNumber + ", " + start + "–" + end.toLocalTime());
        return booking;
    }

    Booking cancel(int id, String who) {
        Booking booking = repository.findById(id)
                .orElseThrow(() -> new BookingException("бронювання #" + id + " не знайдено"));
        if (!booking.who().equals(who)) {
            throw new BookingException("скасувати може лише автор бронювання");
        }
        repository.delete(id);
        mailer.send(who, "Бронювання #" + id + " скасовано");
        return booking;
    }

    List<Booking> list(String roomOrNull) {
        return roomOrNull == null ? repository.findAll() : repository.findByRoom(roomOrNull);
    }
}
