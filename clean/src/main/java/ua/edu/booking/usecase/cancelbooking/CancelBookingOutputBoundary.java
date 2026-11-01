package ua.edu.booking.usecase.cancelbooking;

public interface CancelBookingOutputBoundary {

    record Response(int id) {
    }

    void present(Response response);

    void presentError(String message);
}
