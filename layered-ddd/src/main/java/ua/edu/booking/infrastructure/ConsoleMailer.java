package ua.edu.booking.infrastructure;

import ua.edu.booking.application.Notifier;

public class ConsoleMailer implements Notifier {
    @Override
    public void notify(String who, String message) {
        System.out.println("[EMAIL -> " + who + "] " + message);
    }
}
