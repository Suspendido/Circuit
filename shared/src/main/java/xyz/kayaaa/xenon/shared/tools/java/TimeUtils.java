package xyz.kayaaa.xenon.shared.tools.java;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@UtilityClass
public class TimeUtils {

    public boolean isTime(String input) {
        try {
            parseTime(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public long parseTime(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Invalid time!");
        }

        input = input.trim().toLowerCase();
        long totalMillis = 0L;

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+)(a|mo|w|d|h|m|s)")
                .matcher(input);

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s":  totalMillis += value * 1000L; break;
                case "m":  totalMillis += value * 60_000L; break;
                case "h":  totalMillis += value * 3_600_000L; break;
                case "d":  totalMillis += value * 86_400_000L; break;
                case "w":  totalMillis += value * 604_800_000L; break;
                case "mo": totalMillis += value * 2_629_746_000L; break;
                case "a":  totalMillis += value * 31_556_952_000L; break;
                default: throw new IllegalArgumentException("Invalid time unit: " + unit);
            }
        }

        if (totalMillis == 0) {
            throw new IllegalArgumentException("Invalid time format: " + input);
        }

        return totalMillis;
    }

    public String formatTime(long millis) {
        if (millis < 1000L) {
            return millis + " milliseconds";
        }

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) {
            return years + " year" + (years == 1 ? "" : "s");
        } else if (months > 0) {
            return months + " month" + (months == 1 ? "" : "s");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks == 1 ? "" : "s");
        } else if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }

    public String formatTimeShort(long millis) {
        if (millis < 1000L) {
            return millis + "ms";
        }

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) {
            return years + "y";
        } else if (months > 0) {
            return months + "m";
        } else if (weeks > 0) {
            return weeks + "w";
        } else if (days > 0) {
            return days + "d";
        } else if (hours > 0) {
            return hours + "h";
        } else if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    public String formatDate(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(date);
    }

}
