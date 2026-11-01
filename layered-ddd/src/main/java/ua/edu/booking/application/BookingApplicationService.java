package ua.edu.booking.application;

import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;
import ua.edu.booking.domain.BookingRepository;
import ua.edu.booking.domain.Room;
import ua.edu.booking.domain.RoomRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Шар застосунку (Еванс): тонкий, координує сценарій — знайти, викликати домен, зберегти, повідомити.
 * Власних бізнес-правил не має.
 */
public class BookingApplicationService {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final Notifier notifier;
    private final Clock clock;

    public BookingApplicationService(BookingRepository bookings, RoomRepository rooms, Notifier notifier, Clock clock) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
    }

    public Booking book(String roomNumber, LocalDateTime start, int minutes, String who,
                        int attendees, boolean needsProjector) {
        Room room = rooms.findByNumber(roomNumber)
                .orElseThrow(() -> new BookingException("аудиторії " + roomNumber + " не існує"));
        Booking booking = Booking.create(bookings.nextId(), room, start, minutes, who, attendees, needsProjector,
                LocalDateTime.now(clock));
        if (bookings.findByRoom(roomNumber).stream().anyMatch(booking::overlaps)) {
            throw new BookingException("аудиторія " + roomNumber + " вже зайнята в цей час");
        }
        bookings.save(booking);
        notifier.notify(who, "Бронювання #" + booking.id() + ": ауд. " + roomNumber + ", "
                + booking.start() + "–" + booking.end().toLocalTime());
        return booking;
    }

    public Booking cancel(int id, String who) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new BookingException("бронювання #" + id + " не знайдено"));
        booking.checkCanBeCancelledBy(who);
        bookings.delete(id);
        notifier.notify(who, "Бронювання #" + id + " скасовано");
        return booking;
    }

    public List<Booking> list(Optional<String> room) {
        return room.map(bookings::findByRoom).orElseGet(bookings::findAll);
    }

    public List<Room> rooms() {
        return rooms.findAll();
    }
}
