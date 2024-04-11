package cn.zpl.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
@Slf4j
public class JsonUtil {

    public static JsonElement getFromJson(String str, @NotNull String path) {

        if (str == null || "".equalsIgnoreCase(str)) {
            return JsonNull.INSTANCE;
        }
        String[] paths = path.split("-");
        JsonElement current = parseFromStr(str);
        for (String string : paths) {
            if (current == null) {
                return JsonNull.INSTANCE;
            }
            current = current.getAsJsonObject().get(string);
        }
        return current == null ? JsonNull.INSTANCE : current;
    }

    public static JsonElement getFromJson(JsonElement json, String path) {
        String[] paths = path.split("-");
        JsonElement current = json;
        for (String string : paths) {
            if (current == null) {
                return JsonNull.INSTANCE;
            }
            current = current.getAsJsonObject().get(string);
        }
        return current == null ? JsonNull.INSTANCE : current;
    }

    public static JsonElement parseFromStr(String str) {
        if (str == null || "".equalsIgnoreCase(str)) {
            return JsonNull.INSTANCE;
        }
        try {
            return JsonParser.parseString(str);

        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("解析json失败");
            log.error(str);
        }
        return JsonNull.INSTANCE;
    }

    public static int getFromJson2Integer(JsonElement json, String path) {
        JsonElement result = getFromJson(json, path);
        return result.isJsonNull() ? 0 : result.getAsInt();
    }

    public static String getFromJson2Str(JsonElement json, String path) {
        JsonElement result = getFromJson(json, path);
        return result.isJsonNull() ? "" : (result.isJsonObject() || result.isJsonArray()) ? result.toString() : result.getAsString();
    }
}
