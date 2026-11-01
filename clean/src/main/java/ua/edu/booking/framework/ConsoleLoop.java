package ua.edu.booking.framework;

import ua.edu.booking.adapter.cli.CliController;

import java.util.Scanner;

/** Фреймворк-шар для консолі: лише читання stdin. Уся логіка — в controller. */
public class ConsoleLoop {

    private final CliController controller;

    public ConsoleLoop(CliController controller) {
        this.controller = controller;
    }

    public void run() {
        Scanner in = new Scanner(System.in);
        System.out.println("=== Бронювання аудиторій ===");
        while (true) {
            System.out.print("> ");
            if (!in.hasNextLine() || !controller.handle(in.nextLine())) return;
        }
    }
}
