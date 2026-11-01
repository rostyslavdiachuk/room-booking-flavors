package ua.edu.booking;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Dependency Rule: entity ← usecase ← adapter ← framework. Controller і presenter не бачать entity. */
class ArchitectureTest {

    static final String P = "ua.edu.booking.";

    @Test
    void entitiesDependOnNothing() throws IOException {
        assertEquals(List.of(), violations("entity", P + "usecase", P + "adapter", P + "framework"));
    }

    @Test
    void useCasesDoNotKnowOuterCircles() throws IOException {
        assertEquals(List.of(), violations("usecase", P + "adapter", P + "framework"));
    }

    @Test
    void adaptersDoNotKnowFramework() throws IOException {
        assertEquals(List.of(), violations("adapter", P + "framework"));
    }

    @Test
    void controllersAndPresentersNeverSeeEntities() throws IOException {
        assertEquals(List.of(), violations("adapter/cli", P + "entity"));
        assertEquals(List.of(), violations("adapter/http", P + "entity"));
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
