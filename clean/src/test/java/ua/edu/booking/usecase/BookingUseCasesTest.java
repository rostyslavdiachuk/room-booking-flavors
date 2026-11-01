package ua.edu.booking.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.edu.booking.adapter.gateway.InMemoryBookingGateway;
import ua.edu.booking.adapter.gateway.SeedRoomGateway;
import ua.edu.booking.usecase.bookroom.BookRoomInputBoundary;
import ua.edu.booking.usecase.bookroom.BookRoomInteractor;
import ua.edu.booking.usecase.bookroom.BookRoomOutputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInteractor;
import ua.edu.booking.usecase.cancelbooking.CancelBookingOutputBoundary;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Interactor-и з фейковим presenter-ом: перевіряємо, ЩО use case повідомив, а не як це надруковано. */
class BookingUseCasesTest {

    static final ZoneId ZONE = ZoneId.of("Europe/Kyiv");
    static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 2, 9, 0);
    static final LocalDateTime TOMORROW = NOW.plusDays(1);

    /** Записує все, що use case віддав назовні. */
    static class RecordingPresenter implements BookRoomOutputBoundary, CancelBookingOutputBoundary {
        final List<String> events = new ArrayList<>();

        public void present(BookRoomOutputBoundary.Response r) { events.add("booked #" + r.id()); }
        public void present(CancelBookingOutputBoundary.Response r) { events.add("cancelled #" + r.id()); }
        public void presentError(String message) { events.add("error: " + message); }
    }

    final List<String> sent = new ArrayList<>();
    RecordingPresenter presenter;
    InMemoryBookingGateway bookings;
    BookRoomInteractor bookRoom;
    CancelBookingInteractor cancel;

    @BeforeEach
    void setUp() {
        presenter = new RecordingPresenter();
        bookings = new InMemoryBookingGateway();
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        bookRoom = new BookRoomInteractor(bookings, new SeedRoomGateway(), (who, msg) -> sent.add(who), clock, presenter);
        cancel = new CancelBookingInteractor(bookings, (who, msg) -> sent.add(who), presenter);
    }

    void book(String room, LocalDateTime start, int minutes, String who, int attendees, boolean projector) {
        bookRoom.execute(new BookRoomInputBoundary.Request(room, start, minutes, who, attendees, projector));
    }

    String last() {
        return presenter.events.get(presenter.events.size() - 1);
    }

    @Test
    void booksAndNotifies() {
        book("214", TOMORROW, 90, "Petrenko", 20, false);
        assertEquals("booked #1", last());
        assertEquals(List.of("Petrenko"), sent);
    }

    @Test
    void rejectsUnknownRoom() {
        book("999", TOMORROW, 60, "Koval", 5, false);
        assertEquals("error: аудиторії 999 не існує", last());
    }

    @Test
    void rejectsPast() {
        book("214", NOW.minusMinutes(1), 60, "Koval", 5, false);
        assertEquals("error: не можна бронювати в минулому", last());
    }

    @Test
    void rejectsTooLong() {
        book("214", TOMORROW, 181, "Koval", 5, false);
        assertEquals("error: тривалість має бути від 1 до 180 хв", last());
    }

    @Test
    void rejectsNoAttendees() {
        book("214", TOMORROW, 60, "Koval", 0, false);
        assertEquals("error: кількість осіб має бути додатною", last());
    }

    @Test
    void rejectsOverCapacity() {
        book("305", TOMORROW, 60, "Koval", 16, false);
        assertEquals("error: аудиторія 305 вміщує лише 15 осіб", last());
    }

    @Test
    void rejectsMissingProjector() {
        book("214", TOMORROW, 60, "Koval", 5, true);
        assertEquals("error: в аудиторії 214 немає проєктора", last());
    }

    @Test
    void rejectsOverlapButAllowsBackToBack() {
        book("214", TOMORROW, 60, "Petrenko", 10, false);
        book("214", TOMORROW.plusMinutes(30), 60, "Koval", 5, false);
        assertEquals("error: аудиторія 214 вже зайнята в цей час", last());
        book("214", TOMORROW.plusMinutes(60), 60, "Koval", 5, false);
        assertEquals("booked #2", last());
    }

    @Test
    void onlyAuthorCanCancel() {
        book("214", TOMORROW, 60, "Petrenko", 10, false);
        cancel.execute(new CancelBookingInputBoundary.Request(1, "Koval"));
        assertEquals("error: скасувати може лише автор бронювання", last());
        cancel.execute(new CancelBookingInputBoundary.Request(1, "Petrenko"));
        assertEquals("cancelled #1", last());
        assertEquals(List.of(), bookings.findAll());
    }

    @Test
    void cancelUnknownBooking() {
        cancel.execute(new CancelBookingInputBoundary.Request(42, "Petrenko"));
        assertEquals("error: бронювання #42 не знайдено", last());
    }
}
