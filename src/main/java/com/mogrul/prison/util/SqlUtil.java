package com.mogrul.prison.util;

import com.mogrul.prison.MogrulPrison;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SqlUtil {
    private static final Map<String, String> CACHE = new HashMap<>();

    public static String get(String path) {
        return CACHE.computeIfAbsent(path, p -> {
           try (InputStream in = MogrulPrison.getPlugin(MogrulPrison.class).getResource("sql/" + p)) {
               if (in == null) throw new RuntimeException("Missing sql/" + p);

               return new String(in.readAllBytes(), StandardCharsets.UTF_8);
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
        });
    }
}
