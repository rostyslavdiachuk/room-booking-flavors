package ua.edu.booking.domain;

import java.util.List;
import java.util.Optional;

/** Інтерфейс оголошує домен, реалізацію дає infrastructure (інверсія залежності). */
public interface BookingRepository {

    List<Booking> findAll();

    List<Booking> findByRoom(String room);

    Optional<Booking> findById(int id);

    int nextId();

    void save(Booking booking);

    void delete(int id);
}
