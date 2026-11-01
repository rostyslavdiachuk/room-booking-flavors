package ua.edu.booking.booking;

import ua.edu.booking.notification.Mailer;
import ua.edu.booking.room.RoomCatalog;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Шар представлення фічі «бронювання» — єдиний публічний клас пакета. */
public class BookingCommands {

    private final BookingService service;

    private BookingCommands(BookingService service) {
        this.service = service;
    }

    public static BookingCommands create(RoomCatalog rooms, Mailer mailer) {
        return new BookingCommands(new BookingService(new BookingRepository(Path.of("bookings.csv")), rooms, mailer));
    }

    public void handle(String[] p) {
        try {
            switch (p[0]) {
                case "book" -> book(p);
                case "cancel" -> cancel(p);
                case "list" -> list(p);
                default -> throw new IllegalArgumentException(p[0]);
            }
        } catch (BookingException e) {
            System.out.println("Помилка: " + e.getMessage());
        } catch (DateTimeParseException | NumberFormatException e) {
            System.out.println("Помилка: неправильний формат даних");
        }
    }

    private void book(String[] p) {
        if (p.length < 6 || p.length > 7 || (p.length == 7 && !p[6].equals("projector"))) {
            System.out.println("Формат: book <ауд> <yyyy-MM-ddTHH:mm> <хв> <хто> <осіб> [projector]");
            return;
        }
        Booking b = service.book(p[1], LocalDateTime.parse(p[2]), Integer.parseInt(p[3]), p[4],
                Integer.parseInt(p[5]), p.length == 7);
        System.out.println("OK: #" + b.id() + " ауд. " + b.room() + " " + b.start() + "–" + b.end().toLocalTime());
    }

    private void cancel(String[] p) {
        if (p.length != 3) {
            System.out.println("Формат: cancel <id> <хто>");
            return;
        }
        Booking b = service.cancel(Integer.parseInt(p[1]), p[2]);
        System.out.println("OK: #" + b.id() + " скасовано");
    }

    private void list(String[] p) {
        List<Booking> result = service.list(p.length > 1 ? p[1] : null);
        if (result.isEmpty()) System.out.println("(порожньо)");
        for (Booking b : result) {
            System.out.println("#" + b.id() + " | ауд. " + b.room() + " | " + b.start() + "–" + b.end().toLocalTime()
                    + " | " + b.who() + " | " + b.attendees() + " осіб" + (b.projector() ? " | проєктор" : ""));
        }
    }
}
