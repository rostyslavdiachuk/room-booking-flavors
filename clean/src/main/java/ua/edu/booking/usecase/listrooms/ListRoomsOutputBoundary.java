package ua.edu.booking.usecase.listrooms;

import java.util.List;

public interface ListRoomsOutputBoundary {

    record Item(String number, int capacity, boolean projector) {
    }

    record Response(List<Item> rooms) {
    }

    void present(Response response);
}
