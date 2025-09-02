package xyz.kayaaa.xenon.shared.tools.string;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@UtilityClass
public class StringHelper {

    public String[] splitByNewline(String string) {
        return string.split("\\R");
    }

    public String capitalizeAllWords(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        return Arrays.stream(input.split("\\s+"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";


    public String generateString(int size) {
        return ThreadLocalRandom.current().ints(size, 0, TOKEN_CHARS.length())
                .mapToObj(TOKEN_CHARS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
