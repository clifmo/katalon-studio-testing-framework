package com.kms.katalon.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArrayUtil {
    public interface ArrayFilter <T> {
        public boolean test(T item);
    }

    public static <T> T getFirst(List<T> list) {
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public static <T> int findIndex(T[] list, Object targetItem) {
        if (list == null || targetItem == null) {
            return -1;
        }
        ArrayFilter<T> filter = (item) -> {
            return item == targetItem;
        };
        for (int i = 0; i < list.length; i++) {
            if (filter.test(list[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int findIndex(T[] list, ArrayFilter<T> filter) {
        for (int i = 0; i < list.length; i++) {
            if (filter.test(list[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <T> T find(T[] list, ArrayFilter<T> filter) {
        int index = findIndex(list, filter);
        return index >= 0 ? list[index] : null;
    }

    @SafeVarargs
    public static <T> T getAnyNotNull(T ...list) {
        for (T item : list) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<T, T> toMap(T... array) {
        Map<T, T> map = new HashMap<T, T>();
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length - 1; i += 2) {
                map.put(array[i], array[i + 1]);
            }
        }
        return map;
    }
}
