package ua.edu.booking.booking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Шар даних фічі «бронювання». CSV: id;room;start;end;who;attendees;projector */
class BookingRepository {

    private final Path file;

    BookingRepository(Path file) {
        this.file = file;
    }

    List<Booking> findAll() {
        try {
            if (!Files.exists(file)) return List.of();
            return Files.readAllLines(file).stream().map(BookingRepository::parse).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    List<Booking> findByRoom(String room) {
        return findAll().stream().filter(b -> b.room().equals(room)).toList();
    }

    Optional<Booking> findById(int id) {
        return findAll().stream().filter(b -> b.id() == id).findFirst();
    }

    int nextId() {
        return findAll().stream().mapToInt(Booking::id).max().orElse(0) + 1;
    }

    void save(Booking b) {
        try {
            Files.writeString(file, format(b) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void delete(int id) {
        try {
            Files.write(file, findAll().stream().filter(b -> b.id() != id).map(BookingRepository::format).toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String format(Booking b) {
        return b.id() + ";" + b.room() + ";" + b.start() + ";" + b.end() + ";"
                + b.who() + ";" + b.attendees() + ";" + b.projector();
    }

    private static Booking parse(String line) {
        String[] f = line.split(";");
        return new Booking(Integer.parseInt(f[0]), f[1], LocalDateTime.parse(f[2]), LocalDateTime.parse(f[3]),
                f[4], Integer.parseInt(f[5]), Boolean.parseBoolean(f[6]));
    }
}
