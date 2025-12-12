package com.sylluxpvp.circuit.shared.tools.string;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.Validate;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@UtilityClass
public class StringHelper {

    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

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

    public String generateString(int size) {
        return ThreadLocalRandom.current().ints(size, 0, TOKEN_CHARS.length())
                .mapToObj(TOKEN_CHARS::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    Map<Character, Character> leetMap = new HashMap<>();

    static {
        leetMap.put('0', 'o');
        leetMap.put('1', 'i');
        leetMap.put('2', 'z');
        leetMap.put('3', 'e');
        leetMap.put('4', 'a');
        leetMap.put('5', 's');
        leetMap.put('6', 'g');
        leetMap.put('7', 't');
        leetMap.put('8', 'b');
        leetMap.put('9', 'g');
        leetMap.put('@', 'a');
        leetMap.put('$', 's');
        leetMap.put('!', 'i');
        leetMap.put('+', 't');
        leetMap.put('(', 'c');
        leetMap.put(')', 'c');
        leetMap.put('|', 'i');
        leetMap.put('[', 'c');
        leetMap.put(']', 'c');
    }

    public String clean(String input) {
        Validate.notNull(input, "Input cannot be null");
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        StringBuilder sb = new StringBuilder();
        for (char c : normalized.toCharArray()) {
            if (Character.isLetter(c)) sb.append(c);
            else if (leetMap.containsKey(c)) sb.append(leetMap.get(c));
        }

        return sb.toString();
    }
}
