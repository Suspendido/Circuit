package xyz.kayaaa.xenon.shared.tools;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public class CC {

    public static final String CHAT_BAR = translate("&7&m" + StringUtils.repeat("-", 48));
    public static final String SCORE_BAR = translate("&7&m" + StringUtils.repeat("-", 22));

    /**
     * Translates color codes in a given string.
     * The method replaces all instances of '&' with '§', which is used by Minecraft for color codes.
     *
     * @param string the string to translate
     * @return the translated string with color codes
     */
    public String translate(String string) {
        if (string == null) {
            return null;
        }
        StringBuilder translator = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c == '&') c = '§';
            translator.append(c);
        }
        return translator.toString();
    }

    /**
     * Translates color codes in a given string list.
     * The method replaces all instances of '&' with '§', which is used by Minecraft for color codes.
     *
     * @param strings the list to translate
     * @return the translated string list with color codes
     */
    public List<String> translate(List<String> strings) {
        if (strings == null) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (String string : strings) {
            list.add(CC.translate(string));
        }
        return list;
    }

    /**
     * Translates color codes in a given string array.
     * The method replaces all instances of '&' with '§', which is used by Minecraft for color codes.
     *
     * @param strings the array to translate
     * @return the translated string array with color codes
     */
    public String[] translate(String[] strings) {
        if (strings == null) {
            return null;
        }
        String[] translated = new String[strings.length];
        for (int i = 0; i < strings.length; i++) {
            translated[i] = CC.translate(strings[i]);
        }
        return translated;
    }

    /**
     * Removes all color from a given string
     *
     * @param input the string to strip
     * @return the translated string array with color codes
     */
    public String removeColors(String input) {
        return input == null ? null : Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]").matcher(CC.translate(input)).replaceAll("");
    }

    public String getPointsDisplay(int currentPoints, int maxPoints) {
        currentPoints = Math.max(0, Math.min(currentPoints, maxPoints));

        String earnedPoints = currentPoints > 0 ?
                "&a" + String.join("", Collections.nCopies(currentPoints, "■")) : "";

        String remainingPoints = (maxPoints - currentPoints) > 0 ?
                "&7" + String.join("", Collections.nCopies(maxPoints - currentPoints, "■")) : "";

        return translate(earnedPoints + remainingPoints);
    }

}
