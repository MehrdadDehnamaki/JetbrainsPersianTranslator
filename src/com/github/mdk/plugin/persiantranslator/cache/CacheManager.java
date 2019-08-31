package com.github.mdk.plugin.persiantranslator.cache;

import com.github.mdk.plugin.persiantranslator.translator.Lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CacheManager {

    public static final String CACHE_SEPARATOR = "#";
    private static final String FA_NAME = "fa-cache.txt";
    private static final String EN_NAME = "en-cache.txt";
    private static final String NEW_LINE = "\n";
    private static final String CACHE_PATH;

    private static final Map<String, String> EN_CACHE = new HashMap<>();
    private static final Map<String, String> FA_CACHE = new HashMap<>();
    private static final AtomicBoolean existCashFile = new AtomicBoolean(false);

    private static Path enPath;
    private static Path faPath;


    static {
        String path;
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isEmpty()) {
            path = userHome + File.separator + ".persianTranslatorCache";
        } else {
            path = null;
        }
        CACHE_PATH = path;
        if (CACHE_PATH != null) {
            fillCash();
        }
    }

    private static void fillCash() {
        Path cachePath = Paths.get(CACHE_PATH);
        enPath = Paths.get(CACHE_PATH + File.separator + EN_NAME);
        faPath = Paths.get(CACHE_PATH + File.separator + FA_NAME);
        boolean en = false;
        boolean fa = false;

        if (Files.exists(cachePath)) {
            if (Files.exists(enPath)) {
                en = true;
                try {
                    List<String> lines = Files.readAllLines(enPath);
                    fillCache(lines, Lang.EN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    en = enPath.toFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Files.exists(faPath)) {
                fa = true;
                try {
                    List<String> lines = Files.readAllLines(faPath);
                    fillCache(lines, Lang.FA);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    fa = faPath.toFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                boolean create = cachePath.toFile().mkdirs();
                if (create) {
                    en = enPath.toFile().createNewFile();
                    fa = faPath.toFile().createNewFile();
                }
            } catch (Exception ignore) {
            }
        }

        existCashFile.set(en && fa);
    }


    private static void fillCache(List<String> lines, Lang lang) {
        lines.stream()
                .filter(line -> line != null && !line.isEmpty())
                .filter(line -> line.contains(CACHE_SEPARATOR))
                .filter(line -> line.split(CACHE_SEPARATOR).length == 2)
                .forEach(line -> {
                    try {
                        String[] pair = line.split(CACHE_SEPARATOR);
                        String key = pair[0];
                        String val = pair[1];
                        if (key != null && !key.isEmpty() && val != null && !val.isEmpty()) {
                            if (lang.equals(Lang.EN)) {
                                EN_CACHE.put(key, val);
                            } else if (lang.equals(Lang.FA)) {
                                FA_CACHE.put(key, val);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    static String findFromCash(String key, Lang lang) {
        switch (lang) {
            case EN:
                return EN_CACHE.get(key);
            case FA:
                return FA_CACHE.get(key);
            default:
                return null;
        }
    }

    public static void updateCache(String key, String value, Lang lang) {
        updateCashFile(key, value, lang);
        switch (lang) {
            case EN:
                EN_CACHE.put(key, value);
            case FA:
                FA_CACHE.put(key, value);
            default:
        }
    }


    private static void updateCashFile(String key, String value, Lang lang) {
        if (existCashFile.get() && key.length() > 20) {
            try {
                String line = key + CACHE_SEPARATOR + value + NEW_LINE;
                if (lang.equals(Lang.EN)) {
                    Files.write(enPath, line.getBytes(), StandardOpenOption.APPEND);
                } else if (lang.equals(Lang.FA)) {
                    Files.write(faPath, line.getBytes(), StandardOpenOption.APPEND);
                }
            } catch (Exception ignore) {
            }
        }
    }

}
