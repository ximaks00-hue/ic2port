package dev.ic2port.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ensures {@code en_us.json} and {@code ru_ru.json} define the same translation keys.
 */
class LangParityTest {

    private static final Pattern KEY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:");

    @Test
    void enAndRuKeysMatch() throws IOException {
        Set<String> enKeys = loadKeys("assets/ic2port/lang/en_us.json");
        Set<String> ruKeys = loadKeys("assets/ic2port/lang/ru_ru.json");

        Set<String> missingInRu = new TreeSet<>(enKeys);
        missingInRu.removeAll(ruKeys);

        Set<String> missingInEn = new TreeSet<>(ruKeys);
        missingInEn.removeAll(enKeys);

        Assertions.assertTrue(missingInRu.isEmpty(),
                "Keys missing in ru_ru.json: " + missingInRu);
        Assertions.assertTrue(missingInEn.isEmpty(),
                "Keys missing in en_us.json: " + missingInEn);
    }

    private static Set<String> loadKeys(final String resourcePath) throws IOException {
        try (InputStream stream = LangParityTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            Assertions.assertNotNull(stream, "Missing lang file: " + resourcePath);
            Set<String> keys = new TreeSet<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = KEY_PATTERN.matcher(line);
                    if (matcher.find()) {
                        keys.add(matcher.group(1));
                    }
                }
            }
            return keys;
        }
    }
}
