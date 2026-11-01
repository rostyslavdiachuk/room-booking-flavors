package ua.edu.booking.data;

public class EmailSender {
    public void send(String to, String text) {
        System.out.println("[EMAIL -> " + to + "] " + text);
    }
}
