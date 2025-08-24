package xyz.kayaaa.xenon.shared.tools;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

@UtilityClass
public class Validation {

    public void isTrue(boolean bool, Consumer<Void> consumer) {
        if (!bool) {
            consumer.accept(null);
        }
    }

    public void isFalse(boolean bool, Consumer<Void> consumer) {
        if (bool) {
            consumer.accept(null);
        }
    }

    public <T> T notNull(T t, Consumer<Void> consumer) {
        if (t == null) {
            consumer.accept(null);
        }
        return t;
    }

    public void isTrue(boolean bool, String message) {
        if (!bool) {
            throw new IllegalArgumentException(message);
        }
    }

    public void isFalse(boolean bool, String message) {
        if (bool) {
            throw new IllegalArgumentException(message);
        }
    }

    public <T> T notNull(T t, String message) {
        if (t == null) {
            throw new IllegalArgumentException(message);
        }
        return t;
    }
}
