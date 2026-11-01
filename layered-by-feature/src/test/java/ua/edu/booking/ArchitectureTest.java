package ua.edu.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Межі між фічами: room нічого не знає про booking, booking бачить лише публічний API room.
 * Внутрішні класи фіч (сервіси, репозиторії) — package-private, тож решту стереже компілятор.
 */
class ArchitectureTest {

    static final String P = "ua.edu.booking.";

    @Test
    void roomDoesNotKnowBooking() throws IOException {
        assertEquals(List.of(), violations("room", P + "booking", P + "shell", P + "notification"));
    }

    @Test
    void featuresDoNotKnowShell() throws IOException {
        assertEquals(List.of(), violations("booking", P + "shell"));
    }

    @Test
    void notificationDependsOnNothing() throws IOException {
        assertEquals(List.of(), violations("notification", P + "room", P + "booking", P + "shell"));
    }

    static List<String> violations(String pkg, String... forbidden) throws IOException {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/ua/edu/booking", pkg.split("/")))) {
            return files.filter(f -> f.toString().endsWith(".java"))
                    .flatMap(f -> lines(f).stream()
                            .filter(l -> l.startsWith("import "))
                            .filter(l -> Stream.of(forbidden).anyMatch(x -> l.startsWith("import " + x + ".")))
                            .map(l -> f.getFileName() + ": " + l))
                    .toList();
        }
    }

    static List<String> lines(Path p) {
        try {
            return Files.readAllLines(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
