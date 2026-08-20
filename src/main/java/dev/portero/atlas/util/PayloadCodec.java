package dev.portero.atlas.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class PayloadCodec {

    private PayloadCodec() {
    }

    public static Map<String, String> read(String payload) {
        Map<String, String> values = new LinkedHashMap<>();
        if (payload == null || payload.isBlank()) {
            return values;
        }

        for (String entry : payload.split(";")) {
            int separator = entry.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            values.put(entry.substring(0, separator), entry.substring(separator + 1));
        }
        return values;
    }

    public static String write(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner(";");
        values.forEach((key, value) -> joiner.add(key + "=" + value));
        return joiner.toString();
    }

    public static Map<String, Double> readDoubles(String payload) {
        Map<String, Double> values = new LinkedHashMap<>();
        read(payload).forEach((key, value) -> {
            try {
                values.put(key, Double.parseDouble(value));
            } catch (NumberFormatException ignored) {
                values.put(key, 0.0);
            }
        });
        return values;
    }

    public static String writeDoubles(Map<String, Double> values) {
        Map<String, String> encoded = new LinkedHashMap<>();
        values.forEach((key, value) -> encoded.put(key, Double.toString(value)));
        return write(encoded);
    }
}
