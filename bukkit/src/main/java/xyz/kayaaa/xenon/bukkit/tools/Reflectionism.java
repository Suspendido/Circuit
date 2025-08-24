package xyz.kayaaa.xenon.bukkit.tools;

import xyz.kayaaa.xenon.bukkit.XenonPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Reflectionism {

    private static final String[] COLORS = {
            "\u001B[31m", // Vermelho
            "\u001B[33m", // Amarelo
            "\u001B[32m", // Verde
            "\u001B[36m", // Ciano
            "\u001B[34m", // Azul
            "\u001B[35m"  // Roxo
    };

    private static final String RESET = "\u001B[0m"; // Reseta a cor
    private static final boolean ENABLE_COLORS = false; // Permite desativar cores se necessário

    public static void log(String text) {
        if (ENABLE_COLORS) {
            for (int i = 0; i < text.length(); i++) {
                String color = COLORS[i % COLORS.length]; // Alterna as cores
                XenonPlugin.getInstance().getLogger().info(color + text.charAt(i));
            }
            XenonPlugin.getInstance().getLogger().info(RESET);
        } else {
            XenonPlugin.getInstance().getLogger().info(text);
        }
    }

    // Acessar o valor de um campo (não estático)
    public static Object getValue(Object base, String field) {
        if (base == null) {
            log("[Reflectionism] Base object is null.");
            return null;
        }
        log("[Reflectionism] Attempting to return '" + field + "'s value on " + base.getClass().getName());
        try {
            Field field1 = getField(base.getClass(), field);
            field1.setAccessible(true);
            return field1.get(base);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            log("[Reflectionism] Failed to access field '" + field + "': " + ex.getMessage());
            return null;
        }
    }

    public static <T> T getValue(Object base, String field, Class<T> type) {
        return type.cast(getValue(base, field));
    }

    // Acessar o valor de um campo estático
    public static Object getValue(Class<?> base, String field) {
        if (base == null) {
            log("[Reflectionism] Base class is null.");
            return null;
        }
        log("[Reflectionism] Attempting to return '" + field + "'s value on " + base.getName());
        try {
            Field field1 = getField(base, field);
            field1.setAccessible(true);
            return field1.get(null); // Para campos estáticos
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            log("[Reflectionism] Failed to access field '" + field + "': " + ex.getMessage());
            return null;
        }
    }

    // Modificar o valor de um campo
    public static boolean setValue(Object base, String field, Object value) {
        if (base == null) {
            log("[Reflectionism] Base object is null.");
            return false;
        }
        log("[Reflectionism] Attempting to set '" + field + "'s value on " + base.getClass().getName());
        try {
            Field field1 = getField(base.getClass(), field);
            field1.setAccessible(true);
            field1.set(base, value);
            return true;
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            log("[Reflectionism] Failed to set field '" + field + "': " + ex.getMessage());
            return false;
        }
    }

    // Invocar um método de um objeto (não estático)
    public static Object invokeMethod(Object base, String methodName, Class<?>[] paramTypes, Object... params) {
        if (base == null) {
            log("[Reflectionism] Base object is null.");
            return null;
        }
        log("[Reflectionism] Attempting to invoke method '" + methodName + "' on " + base.getClass().getName());
        try {
            Method method = base.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(base, params);
        } catch (Exception ex) {
            log("[Reflectionism] Failed to invoke method '" + methodName + "': " + ex.getMessage());
            return null;
        }
    }

    // Invocar um método estático de uma classe
    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object... params) {
        if (clazz == null) {
            log("[Reflectionism] Base class is null.");
            return null;
        }
        log("[Reflectionism] Attempting to invoke static method '" + methodName + "' on " + clazz.getName());
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(null, params); // null para métodos estáticos
        } catch (Exception ex) {
            log("[Reflectionism] Failed to invoke static method '" + methodName + "': " + ex.getMessage());
            return null;
        }
    }

    // Acessar e invocar o construtor de uma classe
    public static Object invokeConstructor(Class<?> clazz, Class<?>[] paramTypes, Object... params) {
        if (clazz == null) {
            log("[Reflectionism] Base class is null.");
            return null;
        }
        log("[Reflectionism] Attempting to invoke constructor for " + clazz.getName());
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(params);
        } catch (Exception ex) {
            ex.printStackTrace();
            log("[Reflectionism] Failed to invoke constructor: " + ex.getMessage());
            return null;
        }
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class<?>... paramTypes) {
        if (clazz == null) {
            log("[Reflectionism] Base class is null.");
            return null;
        }
        log("[Reflectionism] Attempting to find constructor for " + clazz.getName());
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (Exception ex) {
            ex.printStackTrace();
            log("[Reflectionism] Failed to find constructor: " + ex.getMessage());
            return null;
        }
    }

    // Método auxiliar para buscar um campo, incluindo superclasses.
    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // Procura na superclasse
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found.");
    }

    // Verifica o tipo de um campo
    public static Class<?> getFieldType(Object base, String field) {
        if (base == null) {
            log("[Reflectionism] Base object is null.");
            return null;
        }
        try {
            Field field1 = getField(base.getClass(), field);
            return field1.getType();
        } catch (NoSuchFieldException ex) {
            log("[Reflectionism] Failed to get field type for '" + field + "': " + ex.getMessage());
            return null;
        }
    }

    public static Class<?> getInnerClass(Class<?> outerClass, String innerClassName) {
        if (outerClass == null) {
            log("[Reflectionism] Outer class is null.");
            return null;
        }
        try {
            for (Class<?> innerClass : outerClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals(innerClassName)) {
                    return innerClass;
                }
            }
            log("[Reflectionism] Inner class '" + innerClassName + "' not found in " + outerClass.getName());
        } catch (Exception ex) {
            log("[Reflectionism] Failed to find inner class '" + innerClassName + "': " + ex.getMessage());
        }
        return null;
    }

    public static Object getInstanceOfInnerClass(Class<?> outerClass, String innerClassName, Class<?>[] paramTypes, Object... params) {
        if (outerClass == null) {
            log("[Reflectionism] Outer class is null.");
            return null;
        }
        try {
            for (Class<?> innerClass : outerClass.getDeclaredClasses()) {
                if (innerClass.getSimpleName().equals(innerClassName)) {
                    Constructor<?> constructor = innerClass.getDeclaredConstructor(paramTypes);
                    constructor.setAccessible(true);
                    return constructor.newInstance(params);
                }
            }
            log("[Reflectionism] Inner class '" + innerClassName + "' not found in " + outerClass.getName());
        } catch (Exception ex) {
            log("[Reflectionism] Failed to instantiate inner class '" + innerClassName + "': " + ex.getMessage());
        }
        return null;
    }

    // Verifica se um campo é público
    public static boolean isFieldPublic(Object base, String field) {
        if (base == null) {
            log("[Reflectionism] Base object is null.");
            return false;
        }
        try {
            Field field1 = getField(base.getClass(), field);
            return java.lang.reflect.Modifier.isPublic(field1.getModifiers());
        } catch (NoSuchFieldException ex) {
            log("[Reflectionism] Failed to check field visibility for '" + field + "': " + ex.getMessage());
            return false;
        }
    }
}
