package ua.edu.booking.adapter.notification;

import ua.edu.booking.usecase.gateway.Notifier;

public class ConsoleEmailNotifier implements Notifier {
    @Override
    public void notify(String who, String message) {
        System.out.println("[EMAIL -> " + who + "] " + message);
    }
}
