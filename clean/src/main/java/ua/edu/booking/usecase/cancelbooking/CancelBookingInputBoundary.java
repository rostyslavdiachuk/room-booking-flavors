package ua.edu.booking.usecase.cancelbooking;

public interface CancelBookingInputBoundary {

    record Request(int id, String who) {
    }

    void execute(Request request);
}
