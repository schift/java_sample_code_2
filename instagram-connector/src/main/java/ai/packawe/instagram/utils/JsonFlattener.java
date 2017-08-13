package aw.pickaae.instagram.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;

public class JsonFlattener {

    public static Map<String, Object> flatten(JsonElement doc) {
        HashMap<String, Object> fields = new HashMap<String, Object>();
        LinkedList<String> keyPath = new LinkedList<String>();
        flatten(doc, keyPath, fields);
        return fields;
    }

    private static void flatten(JsonElement doc, LinkedList<String> keyPath, HashMap<String, Object> result) {
        if (doc == null || doc.isJsonNull()) {
            if (!keyPath.isEmpty()) {
                result.put(getKeyFor(keyPath), "");
            }
        } else if (doc.isJsonObject()) {
            for (Entry<String, JsonElement> e : doc.getAsJsonObject().entrySet()) {
                keyPath.add(e.getKey());
                flatten(e.getValue(), keyPath, result);
                keyPath.removeLast();
            }
        } else if (doc.isJsonArray()) {
            int idx = 0;
            String lastKey = keyPath.isEmpty() ? "array" : keyPath.getLast();
            for (JsonElement e : doc.getAsJsonArray()) {
                if (!keyPath.isEmpty()) {
                    keyPath.removeLast();
                }
                keyPath.add(lastKey + "[" + idx + "]");
                flatten(e, keyPath, result);
                ++idx;
            }
        } else {
            result.put(getKeyFor(keyPath), doc.getAsString());
        }
    }

    private static String getKeyFor(LinkedList<String> keyPath) {
        StringBuilder sb = new StringBuilder();
        for (String s : keyPath) {
            if (sb.length() > 0) {
                sb.append("_");
            }
            sb.append(s);
        }
        return sb.toString();
    }

}
