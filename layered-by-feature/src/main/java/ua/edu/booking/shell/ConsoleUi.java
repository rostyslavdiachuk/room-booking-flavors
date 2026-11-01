package ua.edu.booking.shell;

import ua.edu.booking.booking.BookingCommands;
import ua.edu.booking.room.RoomCommands;

import java.util.Scanner;

/** Тонка оболонка: читає рядок і віддає його потрібній фічі. */
public class ConsoleUi {

    private final RoomCommands rooms;
    private final BookingCommands bookings;

    public ConsoleUi(RoomCommands rooms, BookingCommands bookings) {
        this.rooms = rooms;
        this.bookings = bookings;
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Бронювання аудиторій ===");
        while (true) {
            System.out.print("> ");
            if (!in.hasNextLine()) return;
            String[] p = in.nextLine().trim().split("\\s+");
            switch (p[0]) {
                case "exit" -> {
                    return;
                }
                case "rooms" -> rooms.rooms();
                case "book", "cancel", "list" -> bookings.handle(p);
                default -> System.out.println("Невідома команда");
            }
        }
    }
}
