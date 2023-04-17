package com.mcreater.amclcore.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectUtil {
    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> fields = new Vector<>();
        fields.addAll(Arrays.asList(clazz.getFields()));
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return fields;
    }

    public static Map<Field, Object> getFieldsContent(Object ob, List<Field> fields) {
        return Stream.of(fields.toArray(new Field[0]))
                .collect(Collectors.toMap(field -> field,
                        field -> {
                            try {
                                return field.get(ob);
                            } catch (IllegalAccessException ignored) {

                            }
                            return null;
                        }));
    }
}
