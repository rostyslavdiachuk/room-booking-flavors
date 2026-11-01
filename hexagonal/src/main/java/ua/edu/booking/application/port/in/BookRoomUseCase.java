package ua.edu.booking.application.port.in;

import ua.edu.booking.domain.Booking;

/** Вхідний (driving) порт: що застосунок уміє робити. */
public interface BookRoomUseCase {
    Booking book(BookRoomCommand command);
}
