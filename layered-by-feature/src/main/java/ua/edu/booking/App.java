package ua.edu.booking;

import ua.edu.booking.booking.BookingCommands;
import ua.edu.booking.notification.Mailer;
import ua.edu.booking.room.RoomCatalog;
import ua.edu.booking.room.RoomCommands;
import ua.edu.booking.shell.ConsoleUi;

public class App {
    public static void main(String[] args) {
        RoomCatalog rooms = RoomCatalog.create();
        new ConsoleUi(new RoomCommands(rooms), BookingCommands.create(rooms, new Mailer())).run();
    }
}
