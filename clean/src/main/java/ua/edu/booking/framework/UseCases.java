package ua.edu.booking.framework;

import ua.edu.booking.adapter.http.UseCaseFactory;
import ua.edu.booking.usecase.bookroom.BookRoomInputBoundary;
import ua.edu.booking.usecase.bookroom.BookRoomInteractor;
import ua.edu.booking.usecase.bookroom.BookRoomOutputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInteractor;
import ua.edu.booking.usecase.cancelbooking.CancelBookingOutputBoundary;
import ua.edu.booking.usecase.gateway.BookingGateway;
import ua.edu.booking.usecase.gateway.Notifier;
import ua.edu.booking.usecase.gateway.RoomGateway;
import ua.edu.booking.usecase.listbookings.ListBookingsInputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsInteractor;
import ua.edu.booking.usecase.listbookings.ListBookingsOutputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsInputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsInteractor;
import ua.edu.booking.usecase.listrooms.ListRoomsOutputBoundary;

import java.time.Clock;

/** Збирає interactor-и з шлюзами. Частина composition root. */
public class UseCases implements UseCaseFactory {

    private final BookingGateway bookings;
    private final RoomGateway rooms;
    private final Notifier notifier;
    private final Clock clock;

    public UseCases(BookingGateway bookings, RoomGateway rooms, Notifier notifier, Clock clock) {
        this.bookings = bookings;
        this.rooms = rooms;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public BookRoomInputBoundary bookRoom(BookRoomOutputBoundary presenter) {
        return new BookRoomInteractor(bookings, rooms, notifier, clock, presenter);
    }

    @Override
    public CancelBookingInputBoundary cancelBooking(CancelBookingOutputBoundary presenter) {
        return new CancelBookingInteractor(bookings, notifier, presenter);
    }

    @Override
    public ListBookingsInputBoundary listBookings(ListBookingsOutputBoundary presenter) {
        return new ListBookingsInteractor(bookings, presenter);
    }

    @Override
    public ListRoomsInputBoundary listRooms(ListRoomsOutputBoundary presenter) {
        return new ListRoomsInteractor(rooms, presenter);
    }
}
