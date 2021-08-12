package com.kms.katalon.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import io.netty.util.internal.StringUtil;

public class ObjectUtil {

    public static <T> T invokeGet(Object object, String getterMethodName) {
        return invokeGet(object, getterMethodName, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeGet(Object object, String getterMethodName, T defaultValue) {
        Method getter = findMethod(object, getterMethodName);
        if (getter != null) {
            return valueOrDefault((T) safeInvoke(object, getter), defaultValue);
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T> void invokeSet(Object object, String method, T data) {
        invokeSet(object, method, data,
                (Class<T>) ((ParameterizedType) data.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public static <T> void invokeSet(Object object, String method, T data, Class<T> type) {
        Method setter = findMethod(object, method, type);
        if (setter != null) {
            safeInvoke(object, setter, data);
        }
    }

    @SafeVarargs
    public static <T> boolean hasMethod(Object object, String method, Class<T>... args) {
        return findMethod(object, method, args) != null;
    }

    @SafeVarargs
    public static <T> Method findMethod(Object object, String methodName, Class<T>... args) {
        if (object == null) {
            return null;
        }
        return findMethod(object, object.getClass(), methodName, args);
    }

    @SafeVarargs
    public static <T> Method findMethod(Object object, Class<?> clazz, String methodName, Class<T>... args) {
        if (object == null || clazz == null) {
            return null;
        }
        try {
            Method foundPublicMethod = clazz.getMethod(methodName, args);
            if (foundPublicMethod != null) {
                return foundPublicMethod;
            }
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException error) {
            try {
                Method foundPrivateMethod = clazz.getDeclaredMethod(methodName, args);
                if (foundPrivateMethod != null) {
                    return foundPrivateMethod;
                }
            } catch (NoSuchMethodException | SecurityException error1) {
                // Just skip
            }
        }
        return findMethod(object, clazz.getSuperclass(), methodName, args);
    }

    public static <T> Object safeInvoke(Object object, Method method, Object... args) {
        if (object == null || method == null) {
            return null;
        }
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method.invoke(object, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException error) {
            return null;
        }
    }

    public static String getStringField(Object object, String fieldName) {
        Object fieldValue = getField(object, fieldName, null);
        return fieldValue instanceof String ? (String) fieldValue : StringUtil.EMPTY_STRING;
    }

    public static <T> T getField(Object object, String fieldName) {
        return getField(object, fieldName, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object object, String fieldName, T defaultValue) {
        if (object == null) {
            return defaultValue;
        }
        try {
            Field field = findField(object, fieldName);
            if (field == null) {
                return defaultValue;
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return valueOrDefault((T) field.get(object), defaultValue);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException exception) {
            // Just skip
        }
        return defaultValue;
    }

    public static <T> void setField(Object object, String fieldName, T value) {
        if (object == null) {
            return;
        }
        try {
            Field field = findField(object, fieldName);
            if (field == null) {
                return;
            }
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            field.set(object, value);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException exception) {
            // Just skip
        }
    }

    public static <T> boolean hasField(Object object, String fieldName) {
        if (object == null) {
            return false;
        }
        try {
            Field field = findField(object, fieldName);
            return field != null;
        } catch (SecurityException | IllegalArgumentException exception) {
            return false;
        }
    }

    public static Field findField(Object object, String fieldName) {
        if (object == null) {
            return null;
        }
        return findField(object.getClass(), fieldName);
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null) {
            return null;
        }
        try {
            Field field = clazz.getDeclaredField(fieldName);
            return field != null ? field : findField(clazz.getSuperclass(), fieldName);
        } catch (SecurityException | IllegalArgumentException | NoSuchFieldException exception) {
            return findField(clazz.getSuperclass(), fieldName);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T clone(T source) {
        try {
            return (T) clone(source, source.getClass().newInstance());
        } catch (InstantiationException | IllegalAccessException error) {
            error.printStackTrace();
        }
        return null;
    }

    public static <T1, T2> T2 clone(T1 source, T2 destination) {
        return clone(source, source.getClass(), destination);
    }

    public static <T1, T2> T2 clone(T1 source, Class<?> sourceClass, T2 destination) {
        if (sourceClass.getSuperclass() != null) {
            clone(source, sourceClass.getSuperclass(), destination);
        }
        Field[] fields = sourceClass.getDeclaredFields();
        String fieldName;
        for (Field field : fields) {
            fieldName = field.getName();
            if (hasField(destination, fieldName)) {
                setField(destination, fieldName, getField(source, fieldName));
            }
        }
        return destination;
    }

    @SuppressWarnings("unchecked")
    public static <T> T valueOrDefault(Object value, T defaultValue) {
        return value != null ? (T) value : defaultValue;
    }
}
