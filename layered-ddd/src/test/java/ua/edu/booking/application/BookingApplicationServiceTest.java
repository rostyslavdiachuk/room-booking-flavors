package ua.edu.booking.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;
import ua.edu.booking.domain.BookingRepository;
import ua.edu.booking.domain.Room;
import ua.edu.booking.domain.RoomRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Правила без файлу, без консолі й без реального часу — завдяки інвертованим репозиторіям. */
class BookingApplicationServiceTest {

    static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");
    static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 9, 0);
    static final LocalDateTime TOMORROW = NOW.plusDays(1);

    static class InMemoryBookings implements BookingRepository {
        final List<Booking> items = new ArrayList<>();

        public List<Booking> findAll() { return List.copyOf(items); }
        public List<Booking> findByRoom(String room) { return items.stream().filter(b -> b.room().equals(room)).toList(); }
        public Optional<Booking> findById(int id) { return items.stream().filter(b -> b.id() == id).findFirst(); }
        public int nextId() { return items.size() + 1; }
        public void save(Booking booking) { items.add(booking); }
        public void delete(int id) { items.removeIf(b -> b.id() == id); }
    }

    static class FixedRooms implements RoomRepository {
        final List<Room> rooms = List.of(new Room("214", 25, false), new Room("305", 15, true));

        public List<Room> findAll() { return rooms; }
        public Optional<Room> findByNumber(String n) { return rooms.stream().filter(r -> r.number().equals(n)).findFirst(); }
    }

    final List<String> sent = new ArrayList<>();
    InMemoryBookings bookings;
    BookingApplicationService service;

    @BeforeEach
    void setUp() {
        bookings = new InMemoryBookings();
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        service = new BookingApplicationService(bookings, new FixedRooms(), (who, msg) -> sent.add(who), clock);
    }

    @Test
    void booksAndNotifies() {
        service.book("214", TOMORROW, 90, "Petrenko", 20, false);
        assertEquals(1, bookings.findAll().size());
        assertEquals(List.of("Petrenko"), sent);
    }

    @Test
    void rejectsUnknownRoom() {
        assertThrows(BookingException.class, () -> service.book("999", TOMORROW, 60, "Koval", 5, false));
    }

    @Test
    void rejectsPast() {
        assertThrows(BookingException.class, () -> service.book("214", NOW.minusMinutes(1), 60, "Koval", 5, false));
    }

    @Test
    void rejectsTooLong() {
        assertThrows(BookingException.class, () -> service.book("214", TOMORROW, 181, "Koval", 5, false));
    }

    @Test
    void rejectsNoAttendees() {
        assertThrows(BookingException.class, () -> service.book("214", TOMORROW, 60, "Koval", 0, false));
    }

    @Test
    void rejectsOverCapacity() {
        assertThrows(BookingException.class, () -> service.book("305", TOMORROW, 60, "Koval", 16, false));
    }

    @Test
    void rejectsMissingProjector() {
        assertThrows(BookingException.class, () -> service.book("214", TOMORROW, 60, "Koval", 5, true));
    }

    @Test
    void rejectsOverlapButAllowsBackToBack() {
        service.book("214", TOMORROW, 60, "Petrenko", 10, false);
        assertThrows(BookingException.class, () -> service.book("214", TOMORROW.plusMinutes(30), 60, "Koval", 5, false));
        service.book("214", TOMORROW.plusMinutes(60), 60, "Koval", 5, false);
        assertEquals(2, bookings.findAll().size());
    }

    @Test
    void onlyAuthorCanCancel() {
        Booking b = service.book("214", TOMORROW, 60, "Petrenko", 10, false);
        assertThrows(BookingException.class, () -> service.cancel(b.id(), "Koval"));
        service.cancel(b.id(), "Petrenko");
        assertEquals(List.of(), bookings.findAll());
    }

    @Test
    void cancelUnknownBooking() {
        assertThrows(BookingException.class, () -> service.cancel(42, "Petrenko"));
    }
}
