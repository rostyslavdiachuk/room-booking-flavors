package ua.edu.booking.usecase.gateway;

import ua.edu.booking.entity.Booking;

import java.util.List;
import java.util.Optional;

/** Шлюз до даних, якого потребують use case. Реалізація — в adapter/gateway. */
public interface BookingGateway {

    List<Booking> findAll();

    List<Booking> findByRoom(String room);

    Optional<Booking> findById(int id);

    int nextId();

    void save(Booking booking);

    void delete(int id);
}
