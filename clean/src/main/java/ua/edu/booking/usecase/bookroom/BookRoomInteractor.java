package ua.edu.booking.usecase.bookroom;

import ua.edu.booking.entity.Booking;
import ua.edu.booking.entity.BookingException;
import ua.edu.booking.entity.Room;
import ua.edu.booking.usecase.gateway.BookingGateway;
import ua.edu.booking.usecase.gateway.Notifier;
import ua.edu.booking.usecase.gateway.RoomGateway;

import java.time.Clock;
import java.time.LocalDateTime;

/** Один use case — один клас. Правила застосунку; правила предметної області — в entity. */
public class BookRoomInteractor implements BookRoomInputBoundary {

    private final BookingGateway bookings;
    private final RoomGateway rooms;
    private final Notifier notifier;
    private final Clock clock;
    private final BookRoomOutputBoundary presenter;

    public BookRoomInteractor(BookingGateway bookings, RoomGateway rooms, Notifier notifier, Clock clock,
                              BookRoomOutputBoundary presenter) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
        this.presenter = presenter;
    }

    @Override
    public void execute(Request r) {
        try {
            Room room = rooms.findByNumber(r.room())
                    .orElseThrow(() -> new BookingException("аудиторії " + r.room() + " не існує"));
            Booking booking = Booking.create(bookings.nextId(), room, r.start(), r.minutes(), r.who(),
                    r.attendees(), r.projector(), LocalDateTime.now(clock));
            if (bookings.findByRoom(r.room()).stream().anyMatch(booking::overlaps)) {
                throw new BookingException("аудиторія " + r.room() + " вже зайнята в цей час");
            }
            bookings.save(booking);
            notifier.notify(r.who(), "Бронювання #" + booking.id() + ": ауд. " + booking.room() + ", "
                    + booking.start() + "–" + booking.end().toLocalTime());
            presenter.present(new BookRoomOutputBoundary.Response(booking.id(), booking.room(), booking.start(), booking.end()));
        } catch (BookingException e) {
            presenter.presentError(e.getMessage());
        }
    }
}
