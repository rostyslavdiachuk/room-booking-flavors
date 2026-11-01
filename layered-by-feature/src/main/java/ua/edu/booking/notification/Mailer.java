package ua.edu.booking.notification;

public class Mailer {
    public void send(String to, String text) {
        System.out.println("[EMAIL -> " + to + "] " + text);
    }
}
