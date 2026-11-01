package ua.edu.booking.usecase.listrooms;

import ua.edu.booking.usecase.gateway.RoomGateway;

public class ListRoomsInteractor implements ListRoomsInputBoundary {

    private final RoomGateway rooms;
    private final ListRoomsOutputBoundary presenter;

    public ListRoomsInteractor(RoomGateway rooms, ListRoomsOutputBoundary presenter) {
        this.rooms = rooms;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        presenter.present(new ListRoomsOutputBoundary.Response(rooms.findAll().stream()
                .map(r -> new ListRoomsOutputBoundary.Item(r.number(), r.capacity(), r.projector()))
                .toList()));
    }
}
