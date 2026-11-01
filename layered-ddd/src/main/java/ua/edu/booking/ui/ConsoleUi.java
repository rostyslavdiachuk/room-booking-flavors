package ua.edu.booking.ui;

import ua.edu.booking.application.BookingApplicationService;
import ua.edu.booking.domain.Booking;
import ua.edu.booking.domain.BookingException;
import ua.edu.booking.domain.Room;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUi {

    private final BookingApplicationService service;

    public ConsoleUi(BookingApplicationService service) {
        this.service = service;
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
                    case "rooms" -> service.rooms().forEach(r -> System.out.println(format(r)));
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
        List<Booking> result = service.list(p.length > 1 ? Optional.of(p[1]) : Optional.empty());
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
