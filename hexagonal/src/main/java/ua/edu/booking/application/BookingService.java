package ua.edu.booking.application;

import ua.edu.booking.application.port.in.BookRoomCommand;
import ua.edu.booking.application.port.in.BookRoomUseCase;
import ua.edu.booking.application.port.in.CancelBookingUseCase;
import ua.edu.booking.application.port.in.ListBookingsUseCase;
import ua.edu.booking.application.port.out.BookingRepository;
import ua.edu.booking.application.port.out.Notifier;
import ua.edu.booking.application.port.out.RoomRepository;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;
import ua.edu.booking.domain.Room;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Реалізує вхідні порти, користується вихідними. Не знає ні про консоль, ні про HTTP, ні про CSV. */
public class BookingService implements BookRoomUseCase, CancelBookingUseCase, ListBookingsUseCase {

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final Notifier notifier;
    private final Clock clock;

    public BookingService(BookingRepository bookings, RoomRepository rooms, Notifier notifier, Clock clock) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public Booking book(BookRoomCommand cmd) {
        Room room = rooms.findByNumber(cmd.room())
                .orElseThrow(() -> new BookingException("аудиторії " + cmd.room() + " не існує"));
        Booking booking = Booking.create(bookings.nextId(), room, cmd.start(), cmd.minutes(), cmd.who(),
                cmd.attendees(), cmd.projector(), LocalDateTime.now(clock));
        if (bookings.findByRoom(cmd.room()).stream().anyMatch(booking::overlaps)) {
            throw new BookingException("аудиторія " + cmd.room() + " вже зайнята в цей час");
        }
        bookings.save(booking);
        notifier.notify(cmd.who(), "Бронювання #" + booking.id() + ": ауд. " + booking.room() + ", "
                + booking.start() + "–" + booking.end().toLocalTime());
        return booking;
    }

    @Override
    public Booking cancel(int id, String who) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new BookingException("бронювання #" + id + " не знайдено"));
        booking.checkCanBeCancelledBy(who);
        bookings.delete(id);
        notifier.notify(who, "Бронювання #" + id + " скасовано");
        return booking;
    }

    @Override
    public List<Booking> list(Optional<String> room) {
        return room.map(bookings::findByRoom).orElseGet(bookings::findAll);
    }
}
