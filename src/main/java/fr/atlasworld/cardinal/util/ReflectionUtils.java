package fr.atlasworld.cardinal.util;

import java.lang.reflect.Field;

/**
 * Common utilities that use reflection
 */
public final class ReflectionUtils {

    /**
     * Inject a value into a static field of the provided class.
     *
     * @param target target class.
     * @param field  field to inject the value into.
     * @param value  value to inject.
     * @throws NoSuchFieldException   thrown if the field could not be found.
     * @throws IllegalAccessException thrown if the field could not be modified.
     */
    public static void staticInject(Class<?> target, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field targetField = target.getDeclaredField(field);
        targetField.setAccessible(true);
        targetField.set(null, value);
        targetField.setAccessible(false);
    }

    /**
     * Injects a value into a specified non-static field of the provided object using reflection.
     *
     * @param target the object whose field's value is to be modified.
     * @param field  the name of the field to inject the value into.
     * @param value  the value to inject into the field.
     * @throws NoSuchFieldException   if the specified field does not exist in the target object.
     * @throws IllegalAccessException if the field cannot be accessed or modified.
     */
    public static void inject(Object target, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field targetField = target.getClass().getDeclaredField(field);
        targetField.setAccessible(true);
        targetField.set(target, value);
        targetField.setAccessible(false);
    }

    /**
     * Retrieves the value of a static field from the specified class.
     *
     * @param target the class from which the static field will be retrieved.
     * @param field  the name of the static field to retrieve.
     * @return the value of the retrieved static field.
     * @throws NoSuchFieldException   if the specified field does not exist in the class.
     * @throws IllegalAccessException if the field access is restricted.
     */
    public static Object retrieveStaticFieldValue(Class<?> target, String field) throws NoSuchFieldException, IllegalAccessException {
        Field targetField = target.getDeclaredField(field);
        targetField.setAccessible(true);
        Object value = targetField.get(null);
        targetField.setAccessible(false);
        return value;
    }

    /**
     * Retrieves the value of a specified field from a given target object using reflection.
     *
     * @param target the object from which to retrieve the value of the field.
     * @param field  the name of the field to retrieve the value from.
     * @return the value of the specified field in the target object.
     * @throws NoSuchFieldException   if the specified field does not exist in the target object's class.
     * @throws IllegalAccessException if access to the field is denied.
     */
    public static Object retrieveFieldValue(Object target, String field) throws NoSuchFieldException, IllegalAccessException {
        Field targetField = target.getClass().getDeclaredField(field);
        targetField.setAccessible(true);
        Object value = targetField.get(target);
        targetField.setAccessible(false);
        return value;
    }
}
