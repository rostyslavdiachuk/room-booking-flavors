package ua.edu.booking.adapter.cli;

import ua.edu.booking.usecase.bookroom.BookRoomInputBoundary;
import ua.edu.booking.usecase.cancelbooking.CancelBookingInputBoundary;
import ua.edu.booking.usecase.listbookings.ListBookingsInputBoundary;
import ua.edu.booking.usecase.listrooms.ListRoomsInputBoundary;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/** Controller: перетворює текст команди на Request і викликає use case. Нічого не друкує про результат. */
public class CliController {

    private final BookRoomInputBoundary bookRoom;
    private final CancelBookingInputBoundary cancelBooking;
    private final ListBookingsInputBoundary listBookings;
    private final ListRoomsInputBoundary listRooms;
    private final CliView view;

    public CliController(BookRoomInputBoundary bookRoom, CancelBookingInputBoundary cancelBooking,
                         ListBookingsInputBoundary listBookings, ListRoomsInputBoundary listRooms, CliView view) {
        this.bookRoom = bookRoom;
        this.cancelBooking = cancelBooking;
        this.listBookings = listBookings;
        this.listRooms = listRooms;
        this.view = view;
    }

    /** @return false, якщо користувач хоче вийти */
    public boolean handle(String line) {
        String[] p = line.trim().split("\\s+");
        try {
            switch (p[0]) {
                case "exit" -> {
                    return false;
                }
                case "rooms" -> listRooms.execute();
                case "book" -> book(p);
                case "cancel" -> cancel(p);
                case "list" -> listBookings.execute(new ListBookingsInputBoundary.Request(
                        p.length > 1 ? Optional.of(p[1]) : Optional.empty()));
                default -> view.show("Невідома команда");
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            view.show("Помилка: неправильний формат даних");
        }
        return true;
    }

    private void book(String[] p) {
        if (p.length < 6 || p.length > 7 || (p.length == 7 && !p[6].equals("projector"))) {
            view.show("Формат: book <ауд> <yyyy-MM-ddTHH:mm> <хв> <хто> <осіб> [projector]");
            return;
        }
        bookRoom.execute(new BookRoomInputBoundary.Request(p[1], LocalDateTime.parse(p[2]), Integer.parseInt(p[3]),
                p[4], Integer.parseInt(p[5]), p.length == 7));
    }

    private void cancel(String[] p) {
        if (p.length != 3) {
            view.show("Формат: cancel <id> <хто>");
            return;
        }
        cancelBooking.execute(new CancelBookingInputBoundary.Request(Integer.parseInt(p[1]), p[2]));
    }
}
