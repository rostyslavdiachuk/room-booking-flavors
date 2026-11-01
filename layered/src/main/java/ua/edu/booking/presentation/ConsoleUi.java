package ua.edu.booking.presentation;

import ua.edu.booking.business.BookingException;
import ua.edu.booking.business.BookingService;
import ua.edu.booking.business.RoomService;
import ua.edu.booking.model.Booking;
import ua.edu.booking.model.Room;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleUi {

    private final BookingService bookings;
    private final RoomService rooms;

    public ConsoleUi(BookingService bookings, RoomService rooms) {
        this.bookings = bookings;
        this.rooms = rooms;
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
                    case "rooms" -> rooms.listRooms().forEach(r -> System.out.println(format(r)));
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
        Booking b = bookings.book(p[1], LocalDateTime.parse(p[2]), Integer.parseInt(p[3]), p[4],
                Integer.parseInt(p[5]), p.length == 7);
        System.out.println("OK: #" + b.id() + " ауд. " + b.room() + " " + b.start() + "–" + b.end().toLocalTime());
    }

    private void cancel(String[] p) {
        if (p.length != 3) {
            System.out.println("Формат: cancel <id> <хто>");
            return;
        }
        Booking b = bookings.cancel(Integer.parseInt(p[1]), p[2]);
        System.out.println("OK: #" + b.id() + " скасовано");
    }

    private void list(String[] p) {
        List<Booking> result = bookings.list(p.length > 1 ? p[1] : null);
        if (result.isEmpty()) System.out.println("(порожньо)");
        result.forEach(b -> System.out.println(format(b)));
    }

    private static String format(Room r) {
        return "ауд. " + r.number() + " | " + r.capacity() + " місць | " + (r.projector() ? "проєктор" : "без проєктора");
    }

    private static String format(Booking b) {
        return "#" + b.id() + " | ауд. " + b.room() + " | " + b.start() + "–" + b.end().toLocalTime()
                + " | " + b.who() + " | " + b.attendees() + " осіб" + (b.projector() ? " | проєктор" : "");
    }
}
