package ua.edu.booking.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.edu.booking.adapter.out.persistence.InMemoryBookingRepository;
import ua.edu.booking.adapter.out.persistence.SeedRoomRepository;
import ua.edu.booking.application.port.in.BookRoomCommand;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Use case з підставленими адаптерами: пам'ять замість CSV, список замість пошти, фіксований годинник. */
class BookingServiceTest {

    static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");
    static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 9, 0);
    static final LocalDateTime TOMORROW = NOW.plusDays(1);

    final List<String> sent = new ArrayList<>();
    InMemoryBookingRepository bookings;
    BookingService service;

    @BeforeEach
    void setUp() {
        bookings = new InMemoryBookingRepository();
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        service = new BookingService(bookings, new SeedRoomRepository(), (who, msg) -> sent.add(who), clock);
    }

    static BookRoomCommand cmd(String room, LocalDateTime start, int minutes, String who, int attendees, boolean projector) {
        return new BookRoomCommand(room, start, minutes, who, attendees, projector);
    }

    @Test
    void booksAndNotifies() {
        service.book(cmd("214", TOMORROW, 90, "Petrenko", 20, false));
        assertEquals(1, bookings.findAll().size());
        assertEquals(List.of("Petrenko"), sent);
    }

    @Test
    void rejectsUnknownRoom() {
        assertThrows(BookingException.class, () -> service.book(cmd("999", TOMORROW, 60, "Koval", 5, false)));
    }

    @Test
    void rejectsPast() {
        assertThrows(BookingException.class, () -> service.book(cmd("214", NOW.minusMinutes(1), 60, "Koval", 5, false)));
    }

    @Test
    void rejectsTooLong() {
        assertThrows(BookingException.class, () -> service.book(cmd("214", TOMORROW, 181, "Koval", 5, false)));
    }

    @Test
    void rejectsNoAttendees() {
        assertThrows(BookingException.class, () -> service.book(cmd("214", TOMORROW, 60, "Koval", 0, false)));
    }

    @Test
    void rejectsOverCapacity() {
        assertThrows(BookingException.class, () -> service.book(cmd("305", TOMORROW, 60, "Koval", 16, false)));
    }

    @Test
    void rejectsMissingProjector() {
        assertThrows(BookingException.class, () -> service.book(cmd("214", TOMORROW, 60, "Koval", 5, true)));
    }

    @Test
    void rejectsOverlapButAllowsBackToBack() {
        service.book(cmd("214", TOMORROW, 60, "Petrenko", 10, false));
        assertThrows(BookingException.class, () -> service.book(cmd("214", TOMORROW.plusMinutes(30), 60, "Koval", 5, false)));
        service.book(cmd("214", TOMORROW.plusMinutes(60), 60, "Koval", 5, false));
        assertEquals(2, bookings.findAll().size());
    }

    @Test
    void onlyAuthorCanCancel() {
        Booking b = service.book(cmd("214", TOMORROW, 60, "Petrenko", 10, false));
        assertThrows(BookingException.class, () -> service.cancel(b.id(), "Koval"));
        service.cancel(b.id(), "Petrenko");
        assertEquals(List.of(), bookings.findAll());
    }

    @Test
    void cancelUnknownBooking() {
        assertThrows(BookingException.class, () -> service.cancel(42, "Petrenko"));
    }
}
