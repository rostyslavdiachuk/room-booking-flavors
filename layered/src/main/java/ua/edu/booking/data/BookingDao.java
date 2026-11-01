package ua.edu.booking.data;

import ua.edu.booking.model.Booking;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** CSV-файл: id;room;start;end;who;attendees;projector */
public class BookingDao {

    private static final Path FILE = Path.of("bookings.csv");

    public List<Booking> findAll() {
        try {
            if (!Files.exists(FILE)) return List.of();
            return Files.readAllLines(FILE).stream().map(BookingDao::parse).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Booking> findByRoom(String room) {
        return findAll().stream().filter(b -> b.room().equals(room)).toList();
    }

    public Optional<Booking> findById(int id) {
        return findAll().stream().filter(b -> b.id() == id).findFirst();
    }

    public int nextId() {
        return findAll().stream().mapToInt(Booking::id).max().orElse(0) + 1;
    }

    public void save(Booking b) {
        try {
            Files.writeString(FILE, format(b) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void delete(int id) {
        List<String> rest = findAll().stream().filter(b -> b.id() != id).map(BookingDao::format).toList();
        try {
            Files.write(FILE, rest);
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
