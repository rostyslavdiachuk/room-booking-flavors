package ua.edu.booking;

import ua.edu.booking.business.BookingService;
import ua.edu.booking.business.RoomService;
import ua.edu.booking.presentation.ConsoleUi;

public class App {
    public static void main(String[] args) {
        new ConsoleUi(new BookingService(), new RoomService()).run();
    }
}
