package com.mcreater.amclcore.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mcreater.amclcore.account.AbstractAccount;
import com.mcreater.amclcore.game.GameRepository;
import com.mcreater.amclcore.java.JavaEnvironment;
import com.mcreater.amclcore.model.game.arguments.GameArgumentsModel;
import com.mcreater.amclcore.model.game.rule.GameRuleModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftNameChangeableRequestModel;
import com.mcreater.amclcore.model.oauth.session.MinecraftProfileRequestModel;
import com.mcreater.amclcore.nbtlib.common.tags.AbstractTag;
import com.mcreater.amclcore.util.adapters.*;
import com.mcreater.amclcore.util.date.GMTDate;
import com.mcreater.amclcore.util.date.StandardDate;
import com.mcreater.amclcore.util.hash.Sha1String;
import com.mcreater.amclcore.util.maven.MavenLibName;
import com.mcreater.amclcore.util.url.MinecraftMirroredResourceURL;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonUtil {
    public static final Gson GSON_PARSER = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(UUID.class, UUIDAdapter.INSTANCE)
            .registerTypeAdapter(MinecraftProfileRequestModel.State.class, StateAdapter.INSTANCE)
            .registerTypeAdapter(MinecraftProfileRequestModel.Variant.class, VariantAdapter.INSTANCE)
            .registerTypeAdapter(MinecraftNameChangeableRequestModel.State.class, NameStateAdapter.INSTANCE)
            .registerTypeHierarchyAdapter(AbstractAccount.class, AbstractAccountAdapter.INSTANCE)
            .registerTypeAdapter(StandardDate.class, StandardDateAdapter.INSTANCE)
            .registerTypeAdapter(GameArgumentsModel.GameArgumentsItem.class, GameArgumentsItemJsonDeserializer.INSTANCE)
            .registerTypeAdapter(GameArgumentsModel.GameArgumentsItem.class, GameArgumentsItemJsonSerializer.INSTANCE)
            .registerTypeAdapter(GameRuleModel.Action.class, GameRuleStateAdapter.INSTANCE)
            .registerTypeAdapter(Pattern.class, PatternAdapter.INSTANCE)
            .registerTypeAdapter(GMTDate.class, GMTDateAdapter.INSTANCE)
            .registerTypeAdapter(Sha1String.class, Sha1StringAdapter.INSTANCE)
            .registerTypeAdapter(MinecraftMirroredResourceURL.class, MinecraftMirroredResourceURLAdapter.INSTANCE)
            .registerTypeAdapter(GameRepository.class, GameRepositoryAdapter.INSTANCE)
            .registerTypeAdapter(JavaEnvironment.class, JavaEnvironmentAdapter.INSTANCE)
            .registerTypeAdapter(MemorySizeAdapter.class, MemorySizeAdapter.INSTANCE)
            .registerTypeAdapter(MavenLibName.class, MavenLibNameAdapter.INSTANCE)
            .registerTypeAdapter(AbstractTag.class, AbstractTagSerializer.INSTANCE)
            .disableHtmlEscaping()
            .setLenient()
            .create();
    private static final Pattern NAME_PATTERN = Pattern.compile("\"name\":\\s*\"(.+?)\"");
    private static final Pattern UUID_PATTERN = Pattern.compile("\"id\":\\s*\"(.+?)\"");

    public static String toJson(Object object) {
        return GSON_PARSER.toJson(object);
    }

    @SafeVarargs
    public static <T> List<T> createList(T... value) {
        return Arrays.stream(value)
                .collect(Collectors.toList());
    }

    public static NameValuePair createPair(String key, String value) {
        return new BasicNameValuePair(key, value);
    }

    public static <T, V> Map.Entry<T, V> pair(T t, V v) {
        return new ImmutablePair<>(t, v);
    }

    @SafeVarargs
    public static <T, V> Map<T, V> map(Map.Entry<T, V>... p) {
        Map<T, V> map = new HashMap<T, V>();
        Arrays.stream(p).forEach(a -> map.put(a.getKey(), a.getValue()));
        return map;
    }

    public static <T, V> Map<T, V> map(List<Map.Entry<T, V>> p) {
        Map<T, V> map = new HashMap<>();
        p.forEach(a -> map.put(a.getKey(), a.getValue()));
        return map;
    }

    public static Map<String, Object> createSingleMap(String s, Object o) {
        return map(pair(s, o));
    }

    public static abstract class JsonProcessor {
        JsonReader reader;
        Stack<Object> objectStack = new Stack<>();
        String name;

        public JsonProcessor(JsonReader reader) {
            this.reader = reader;
        }

        void putValue(Object o) {
            Object cont = objectStack.pop();
            objectStack.push(cont);
            if (cont instanceof Collection<?>) {
                ((Collection<Object>) cont).add(o);
            } else if (cont instanceof Map<?, ?>) {
                ((Map<String, Object>) cont).put(name, o);
            }
            name = "null";
        }

        public void process() throws IOException {
            JsonToken token = reader.peek();
            switch (token) {
                case NULL:
                    reader.nextNull();
                    putValue(null);
                    break;
                case NAME:
                    name = reader.nextName();
                    break;
                case STRING:
                    putValue(reader.nextString());
                    break;
                case NUMBER:
                    try {
                        putValue(reader.nextLong());
                    } catch (Exception e) {
                        putValue(reader.nextDouble());
                    }
                    break;
                case BOOLEAN:
                    putValue(reader.nextBoolean());
                    break;
                case BEGIN_OBJECT:
                    reader.beginObject();
                    MappedJson content2 = new MappedJson();
                    putValue(content2);
                    objectStack.push(content2);
                    break;
                case END_OBJECT:
                    reader.endObject();
                    objectStack.pop();
                    break;
                case BEGIN_ARRAY:
                    reader.beginArray();
                    ListedJson content3 = new ListedJson();
                    putValue(content3);
                    objectStack.push(content3);
                    break;
                case END_ARRAY:
                    reader.endArray();
                    objectStack.pop();
                    break;
                default:
                case END_DOCUMENT:
                    break;
            }
        }

        public boolean processable() {
            return objectStack.size() > 0;
        }

        public abstract Object getProcessedContent();
    }

    public static class JsonToCollectionProcessor extends JsonProcessor {
        private final ListedJson content = new ListedJson();

        public JsonToCollectionProcessor(JsonReader reader) throws IOException {
            super(reader);
            if (reader.peek() == JsonToken.BEGIN_ARRAY) {
                objectStack.push(content);
                reader.beginArray();
            } else {
                throw new IOException("Not a array");
            }
        }

        public ListedJson getProcessedContent() {
            return content;
        }
    }

    public static class JsonToMapProcessor extends JsonProcessor {
        private final MappedJson content = new MappedJson();

        public JsonToMapProcessor(JsonReader reader) throws IOException {
            super(reader);
            this.reader = reader;
            if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                objectStack.push(content);
                reader.beginObject();
            } else {
                throw new IOException("Not a map");
            }
        }

        public MappedJson getProcessedContent() {
            return content;
        }
    }

    public interface JsonStruct {
        Object getObject(String... path);

        default int tryGetInteger(String... path) {
            Object temp = getObject(path);
            if (temp == null) return 0;
            if (temp instanceof Number) return ((Number) temp).intValue();
            if (temp instanceof String) return Integer.parseInt((String) temp);
            return 0;
        }

        default int tryGetInteger(int def, String... path) {
            Object temp = getObject(path);
            if (temp == null) return def;
            if (temp instanceof Number) return ((Number) temp).intValue();
            if (temp instanceof String) return Integer.parseInt((String) temp);
            return def;
        }

        default long tryGetLong(String... path) {
            Object temp = getObject(path);
            if (temp == null) return 0;
            if (temp instanceof Number) return ((Number) temp).longValue();
            if (temp instanceof String) return Long.parseLong((String) temp);
            return 0;
        }

        default float tryGetFloat(String... path) {
            Object temp = getObject(path);
            if (temp == null) return 0;
            if (temp instanceof Number) return ((Number) temp).floatValue();
            if (temp instanceof String) return Float.parseFloat((String) temp);
            return 0;
        }

        default double tryGetDouble(String... path) {
            Object temp = getObject(path);
            if (temp == null) return 0;
            if (temp instanceof Number) return ((Number) temp).doubleValue();
            if (temp instanceof String) return Double.parseDouble((String) temp);
            return 0;
        }

        default boolean tryGetBoolean(String... path) {
            Object temp = getObject(path);
            if (temp == null) return false;
            if (temp instanceof Boolean) return (Boolean) temp;
            if (temp instanceof String) return Boolean.parseBoolean((String) temp);
            return false;
        }

        default String tryGetString(String... path) {
            Object temp = getObject(path);
            if (temp instanceof String) return (String) temp;
            return null;
        }

        default ListedJson tryGetList(String... path) {
            Object temp = getObject(path);
            if (temp == null) return null;
            if (temp instanceof ListedJson) return (ListedJson) temp;
            return new ListedJson();
        }

        default MappedJson tryGetMap(String... path) {
            Object temp = getObject(path);
            if (temp == null) return null;
            if (temp instanceof MappedJson) return (MappedJson) temp;
            return new MappedJson();
        }
    }

    public static class MappedJson extends HashMap<String, Object> implements JsonStruct {
        public Object getObject(String... path) {
            Object temp = this;
            for (String part : path) {
                if (temp instanceof MappedJson) temp = ((MappedJson) temp).get(part);
                else if (temp instanceof ListedJson && parseQuotedIndex(part) >= 0)
                    temp = ((ListedJson) temp).get(parseQuotedIndex(part));
                else return null;
            }
            return temp;
        }
    }

    public static class ListedJson extends Vector<Object> implements JsonStruct {
        public Object getObject(String... path) {
            Object temp = this;
            for (String part : path) {
                if (temp instanceof MappedJson) temp = ((MappedJson) temp).get(part);
                else if (temp instanceof ListedJson && parseQuotedIndex(part) >= 0)
                    temp = ((ListedJson) temp).get(parseQuotedIndex(part));
                else return null;
            }
            return temp;
        }
    }

    public static int parseQuotedIndex(String s) {
        if (s.charAt(0) != '[' || s.charAt(s.length() - 1) != ']') return -1;
        try {
            return Integer.parseInt(s.replace("[", "").replace("]", ""));
        } catch (Exception e) {
            return -1;
        }
    }
}
