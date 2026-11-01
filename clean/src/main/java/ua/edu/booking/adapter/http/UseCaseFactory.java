package ua.edu.booking.adapter.http;

import ua.edu.booking.usecase.bookroom.BookRoomInputBoundary;
import ua.edu.booking.usecase.bookroom.BookRoomOutputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingOutputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsInputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsOutputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsInputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsOutputBoundary;

/** На кожен HTTP-запит — свій presenter, тож use case створюємо на льоту. Реалізацію дає framework. */
public interface UseCaseFactory {

    BookRoomInputBoundary bookRoom(BookRoomOutputBoundary presenter);

    CancelBookingInputBoundary cancelBooking(CancelBookingOutputBoundary presenter);

    ListBookingsInputBoundary listBookings(ListBookingsOutputBoundary presenter);

    ListRoomsInputBoundary listRooms(ListRoomsOutputBoundary presenter);
}
