package ua.edu.booking.adapter.gateway;

import ua.edu.booking.entity.Booking;
import ua.edu.booking.usecase.gateway.BookingGateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Ще одна реалізація того самого шлюзу — для тестів і демо. */
public class InMemoryBookingGateway implements BookingGateway {

    private final List<Booking> items = new ArrayList<>();

    @Override
    public List<Booking> findAll() {
        return List.copyOf(items);
    }

    @Override
    public List<Booking> findByRoom(String room) {
        return items.stream().filter(b -> b.room().equals(room)).toList();
    }

    @Override
    public Optional<Booking> findById(int id) {
        return items.stream().filter(b -> b.id() == id).findFirst();
    }

    @Override
    public int nextId() {
        return items.stream().mapToInt(Booking::id).max().orElse(0) + 1;
    }

    @Override
    public void save(Booking booking) {
        items.add(booking);
    }

    @Override
    public void delete(int id) {
        items.removeIf(b -> b.id() == id);
    }
}
