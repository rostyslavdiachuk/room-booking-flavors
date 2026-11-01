import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Метрики компонентів за Робертом Мартіном (Clean Architecture, ч. IV).
 * Компонент = Java-пакет. Залежність = import класу з іншого пакета проєкту.
 *
 *   Ca — скільки класів ззовні залежать від цього пакета (вхідні)
 *   Ce — скільки класів цього пакета залежать від інших (вихідні)
 *   I  = Ce / (Ca + Ce)      нестабільність: 0 — стабільний, 1 — нестабільний
 *   A  = абстрактні / усі     абстрактність
 *   D  = |A + I - 1|          відстань від головної послідовності
 *
 * Запуск (JDK 21, без компіляції):
 *   java tools/PackageMetrics.java src/main/java
 */
public class PackageMetrics {

    record JavaClass(String pkg, String name, boolean isAbstract, Set<String> importedPackages) {}

    private static final Pattern PACKAGE = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);
    private static final Pattern IMPORT = Pattern.compile("^import\\s+(?!static)([\\w.]+)\\.[A-Z]\\w*;", Pattern.MULTILINE);
    private static final Pattern ABSTRACT = Pattern.compile("^public\\s+(?:sealed\\s+)?(interface|abstract\\s+class)\\b", Pattern.MULTILINE);

    public static void main(String[] args) throws IOException {
        Path root = Path.of(args.length > 0 ? args[0] : "src/main/java");
        List<JavaClass> classes = new ArrayList<>();
        try (Stream<Path> files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                classes.add(parse(file));
            }
        }

        Set<String> packages = new TreeSet<>();
        classes.forEach(c -> packages.add(c.pkg()));
        String prefix = commonPrefix(packages);

        // Вихідні залежності кожного класу на інші пакети проєкту
        Map<JavaClass, Set<String>> deps = new LinkedHashMap<>();
        for (JavaClass c : classes) {
            Set<String> out = new TreeSet<>(c.importedPackages());
            out.retainAll(packages);
            out.remove(c.pkg());
            deps.put(c, out);
        }

        System.out.printf("%-34s %3s %3s %5s %5s %5s%n", "Компонент", "Ca", "Ce", "I", "A", "D");
        System.out.println("-".repeat(60));
        for (String pkg : packages) {
            long total = classes.stream().filter(c -> c.pkg().equals(pkg)).count();
            long abstracts = classes.stream().filter(c -> c.pkg().equals(pkg) && c.isAbstract()).count();
            long ce = deps.entrySet().stream()
                    .filter(e -> e.getKey().pkg().equals(pkg) && !e.getValue().isEmpty()).count();
            long ca = deps.entrySet().stream()
                    .filter(e -> !e.getKey().pkg().equals(pkg) && e.getValue().contains(pkg)).count();
            double i = ca + ce == 0 ? 0 : (double) ce / (ca + ce);
            double a = (double) abstracts / total;
            double d = Math.abs(a + i - 1);
            System.out.printf("%-34s %3d %3d %5.2f %5.2f %5.2f%n", shortName(pkg, prefix), ca, ce, i, a, d);
        }

        // Граф пакетів і пошук циклів (ADP)
        Map<String, Set<String>> graph = new TreeMap<>();
        packages.forEach(p -> graph.put(p, new TreeSet<>()));
        deps.forEach((c, out) -> graph.get(c.pkg()).addAll(out));

        System.out.println();
        System.out.println("Залежності:");
        graph.forEach((from, to) -> to.forEach(t ->
                System.out.println("  " + shortName(from, prefix) + " -> " + shortName(t, prefix))));

        List<List<String>> cycles = findCycles(graph);
        System.out.println();
        if (cycles.isEmpty()) {
            System.out.println("Циклів немає (ADP виконано)");
        } else {
            System.out.println("ЦИКЛИ (порушено ADP):");
            for (List<String> cycle : cycles) {
                System.out.println("  " + String.join(" <-> ", cycle.stream().map(p -> shortName(p, prefix)).toList()));
            }
        }
    }

    private static JavaClass parse(Path file) throws IOException {
        String src = Files.readString(file);
        Matcher pm = PACKAGE.matcher(src);
        String pkg = pm.find() ? pm.group(1) : "";
        Set<String> imports = new TreeSet<>();
        Matcher im = IMPORT.matcher(src);
        while (im.find()) imports.add(im.group(1));
        String name = file.getFileName().toString().replace(".java", "");
        return new JavaClass(pkg, name, ABSTRACT.matcher(src).find(), imports);
    }

    /** Сильно зв'язні компоненти (Тарʼян) розміром більше одного пакета = цикли. */
    private static List<List<String>> findCycles(Map<String, Set<String>> graph) {
        List<List<String>> result = new ArrayList<>();
        Map<String, Integer> index = new HashMap<>(), low = new HashMap<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        int[] counter = {0};
        for (String v : graph.keySet()) {
            if (!index.containsKey(v)) strongConnect(v, graph, index, low, stack, onStack, counter, result);
        }
        return result;
    }

    private static void strongConnect(String v, Map<String, Set<String>> graph, Map<String, Integer> index,
                                      Map<String, Integer> low, Deque<String> stack, Set<String> onStack,
                                      int[] counter, List<List<String>> result) {
        index.put(v, counter[0]);
        low.put(v, counter[0]);
        counter[0]++;
        stack.push(v);
        onStack.add(v);
        for (String w : graph.get(v)) {
            if (!index.containsKey(w)) {
                strongConnect(w, graph, index, low, stack, onStack, counter, result);
                low.put(v, Math.min(low.get(v), low.get(w)));
            } else if (onStack.contains(w)) {
                low.put(v, Math.min(low.get(v), index.get(w)));
            }
        }
        if (low.get(v).equals(index.get(v))) {
            List<String> component = new ArrayList<>();
            String w;
            do {
                w = stack.pop();
                onStack.remove(w);
                component.add(w);
            } while (!w.equals(v));
            if (component.size() > 1) {
                Collections.sort(component);
                result.add(component);
            }
        }
    }

    private static String commonPrefix(Set<String> packages) {
        if (packages.size() < 2) return "";
        String first = packages.iterator().next();
        String prefix = first;
        for (String p : packages) {
            while (!(p.equals(prefix) || p.startsWith(prefix + "."))) {
                int dot = prefix.lastIndexOf('.');
                if (dot < 0) return "";
                prefix = prefix.substring(0, dot);
            }
        }
        return prefix;
    }

    private static String shortName(String pkg, String prefix) {
        if (prefix.isEmpty() || pkg.equals(prefix)) return pkg.equals(prefix) && !prefix.isEmpty() ? "(root)" : pkg;
        return pkg.substring(prefix.length() + 1);
    }
}
