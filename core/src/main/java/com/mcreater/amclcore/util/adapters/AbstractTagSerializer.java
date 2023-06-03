package com.mcreater.amclcore.util.adapters;

import com.google.gson.*;
import com.mcreater.amclcore.nbtlib.common.TagType;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;

import java.lang.reflect.Type;
import java.util.Arrays;

public class AbstractTagSerializer implements JsonSerializer<AbstractTag<?>> {
    public static final AbstractTagSerializer INSTANCE = new AbstractTagSerializer();

    private AbstractTagSerializer() {
    }

    public JsonElement serialize(AbstractTag<?> src, Type typeOfSrc, JsonSerializationContext context) {
        TagType tg = TagType.search(src.getClass());
        if (tg == null) return JsonNull.INSTANCE;
        switch (tg) {
            default:
            case END_TAG:
                return null;
            case BYTE_TAG:
                return new JsonPrimitive(src.toNumberTag().asByte());
            case SHORT_TAG:
                return new JsonPrimitive(src.toNumberTag().asShort());
            case INTEGER_TAG:
                return new JsonPrimitive(src.toNumberTag().asInteger());
            case LONG_TAG:
                return new JsonPrimitive(src.toNumberTag().asLong());
            case FLOAT_TAG:
                return new JsonPrimitive(src.toNumberTag().asFloat());
            case DOUBLE_TAG:
                return new JsonPrimitive(src.toNumberTag().asDouble());
            case BYTE_ARRAY_TAG:
                JsonArray arrb = new JsonArray(src.toByteArrayTag().size());
                Arrays.stream(src.toByteArrayTag().getValue()).forEach(arrb::add);
                return arrb;
            case STRING_TAG:
                return new JsonPrimitive(src.toStringTag().getValue());
            case LIST_TAG:
                JsonArray arrl = new JsonArray();
                src.toListTag(AbstractTag.class).forEach(abstractTag -> arrl.add(context.serialize(abstractTag)));
                return arrl;
            case COMPOUND_TAG:
                JsonObject obj = new JsonObject();
                src.toCompoundTag().forEach((s, abstractTag) -> obj.add(s, context.serialize(abstractTag)));
                return obj;
            case INTEGER_ARRAY_TAG:
                JsonArray arri = new JsonArray(src.toIntegerArrayTag().size());
                Arrays.stream(src.toIntegerArrayTag().getValue()).forEach(arri::add);
                return arri;
            case LONG_ARRAY_TAG:
                JsonArray arrla = new JsonArray(src.toLongArrayTag().size());
                Arrays.stream(src.toLongArrayTag().getValue()).forEach(arrla::add);
                return arrla;
        }
    }
}
