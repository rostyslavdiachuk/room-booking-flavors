package ua.edu.booking.adapter.in.cli;

import ua.edu.booking.adapter.in.TextFormat;
import ua.edu.booking.application.port.in.BookRoomCommand;
import ua.edu.booking.application.port.in.BookRoomUseCase;
import ua.edu.booking.application.port.in.CancelBookingUseCase;
import ua.edu.booking.application.port.in.ListBookingsUseCase;
import ua.edu.booking.application.port.in.ListRoomsUseCase;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Вхідний адаптер: текст консолі → виклики use case. */
public class ConsoleUi {

    private final BookRoomUseCase bookRoom;
    private final CancelBookingUseCase cancelBooking;
    private final ListBookingsUseCase listBookings;
    private final ListRoomsUseCase listRooms;

    public ConsoleUi(BookRoomUseCase bookRoom, CancelBookingUseCase cancelBooking,
                     ListBookingsUseCase listBookings, ListRoomsUseCase listRooms) {
        this.bookRoom = bookRoom;
        this.cancelBooking = cancelBooking;
        this.listBookings = listBookings;
        this.listRooms = listRooms;
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Бронювання аудиторій ===");
        while (true) {
            System.out.print("> ");
            if (!in.hasNextLine()) return;
            String[] p = in.nextLine().trim().split("\\s+");
            try {
                switch (p[0]) {
                    case "exit" -> {
                        return;
                    }
                    case "rooms" -> listRooms.listRooms().forEach(r -> System.out.println(TextFormat.room(r)));
                    case "book" -> book(p);
                    case "cancel" -> cancel(p);
                    case "list" -> list(p);
                    default -> System.out.println("Невідома команда");
                }
            } catch (BookingException e) {
                System.out.println("Помилка: " + e.getMessage());
            } catch (DateTimeParseException | NumberFormatException e) {
                System.out.println("Помилка: неправильний формат даних");
            }
        }
    }

    private void book(String[] p) {
        if (p.length < 6 || p.length > 7 || (p.length == 7 && !p[6].equals("projector"))) {
            System.out.println("Формат: book <ауд> <yyyy-MM-ddTHH:mm> <хв> <хто> <осіб> [projector]");
            return;
        }
        Booking b = bookRoom.book(new BookRoomCommand(p[1], LocalDateTime.parse(p[2]), Integer.parseInt(p[3]),
                p[4], Integer.parseInt(p[5]), p.length == 7));
        System.out.println(TextFormat.booked(b));
    }

    private void cancel(String[] p) {
        if (p.length != 3) {
            System.out.println("Формат: cancel <id> <хто>");
            return;
        }
        Booking b = cancelBooking.cancel(Integer.parseInt(p[1]), p[2]);
        System.out.println("OK: #" + b.id() + " скасовано");
    }

    private void list(String[] p) {
        List<Booking> result = listBookings.list(p.length > 1 ? Optional.of(p[1]) : Optional.empty());
        if (result.isEmpty()) System.out.println("(порожньо)");
        result.forEach(b -> System.out.println(TextFormat.booking(b)));
    }
}
