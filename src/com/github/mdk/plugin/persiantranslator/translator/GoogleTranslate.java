package com.github.mdk.plugin.persiantranslator.translator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GoogleTranslate {

    static final String CONNECTION_ERROR = "!@#$%^&*()_+";
    private static final String URL_ENCODE = "UTF-8";
    private static final String USER_AGENT_NAME = "User-Agent";
    private static final String GOOGLE_ADDRESS = "http://www.google.com";
    private static final String USER_AGENT_VALUE = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:2.0) Gecko/20100101 Firefox/4.0";
    private static final String BASE_URL_FORMAT_EN_TO_FA = "http://translate.google.com/translate_a/single?client=webapp&hl=en&sl=en&tl=fa&q=%s&multires=1&otf=0&pc=0&trs=1&ssel=0&tsel=0&kc=1&dt=t&ie=UTF-8&oe=UTF-8&tk=%s";
    private static final String BASE_URL_FORMAT_FA_TO_EN = "http://translate.google.com/translate_a/single?client=webapp&hl=en&sl=fa&tl=en&q=%s&multires=1&otf=0&pc=0&trs=1&ssel=0&tsel=0&kc=1&dt=t&ie=UTF-8&oe=UTF-8&tk=%s";


    public GoogleTranslate() {
    }

    String enToFa(String text) throws IOException {
        return translateUrl(String.format(BASE_URL_FORMAT_EN_TO_FA, encodeText(text), generateToken(text)));
    }

    String faToEn(String text) throws IOException {
        return translateUrl(String.format(BASE_URL_FORMAT_FA_TO_EN, encodeText(text), generateToken(text)));
    }

    private String translateUrl(String urlText) {
        try {
            URL url = new URL(urlText);
            String rawData = urlToText(url);
            String[] raw = rawData.split("\"");
            if (raw.length < 2) {
                return null;
            }
            return raw[1];
        } catch (UnknownHostException e) {
            return isOnline() ? null : CONNECTION_ERROR;
        } catch (Exception e) {
            return null;
        }
    }

    private String encodeText(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, URL_ENCODE);
    }

    private String urlToText(URL url) throws IOException {
        URLConnection urlConn = url.openConnection();
        urlConn.addRequestProperty(USER_AGENT_NAME, USER_AGENT_VALUE);
        Reader r = new InputStreamReader(urlConn.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder buf = new StringBuilder();
        while (true) {
            int ch = r.read();
            if (ch < 0) {
                break;
            }
            buf.append((char) ch);
        }
        r.close();
        return buf.toString();
    }


    private int[] TKK() {
        return new int[]{0x6337E, 0x217A58DC + 0x5AF91132};
    }


    private int shr32(int x, int bits) {
        if (x < 0) {
            long x_l = 0xffffffffL + x + 1;
            return (int) (x_l >> bits);
        }
        return x >> bits;
    }

    private int RL(int a, String b) {
        for (int c = 0; c < b.length() - 2; c += 3) {
            int d = b.charAt(c + 2);
            d = d >= 65 ? d - 87 : d - 48;
            d = b.charAt(c + 1) == '+' ? shr32(a, d) : (a << d);
            a = b.charAt(c) == '+' ? (a + (d)) : a ^ d;
        }
        return a;
    }

    private String generateToken(String text) {
        int[] tkk = TKK();
        int b = tkk[0];
        int e = 0;
        int f = 0;
        List<Integer> d = new ArrayList<>();
        for (; f < text.length(); f++) {
            int g = text.charAt(f);
            if (0x80 > g) {
                d.add(e++, g);
            } else {
                if (0x800 > g) {
                    d.add(e++, g >> 6 | 0xC0);
                } else {
                    if (0xD800 == (g & 0xFC00) && f + 1 < text.length() && 0xDC00 == (text.charAt(f + 1) & 0xFC00)) {
                        g = 0x10000 + ((g & 0x3FF) << 10) + (text.charAt(++f) & 0x3FF);
                        d.add(e++, g >> 18 | 0xF0);
                        d.add(e++, g >> 12 & 0x3F | 0x80);
                    } else {
                        d.add(e++, g >> 12 | 0xE0);
                        d.add(e++, g >> 6 & 0x3F | 0x80);
                    }
                }
                d.add(e++, g & 63 | 128);
            }
        }

        int a_i = b;
        for (e = 0; e < d.size(); e++) {
            a_i += d.get(e);
            a_i = RL(a_i, "+-a^+6");
        }
        a_i = RL(a_i, "+-3^+b+-f");
        a_i ^= tkk[1];
        long a_l;
        if (0 > a_i) {
            a_l = 0x80000000L + (a_i & 0x7FFFFFFF);
        } else {
            a_l = a_i;
        }
        a_l %= Math.pow(10, 6);
        return String.format(Locale.US, "%d.%d", a_l, a_l ^ b);
    }


    private boolean isOnline() {
        try {
            URL url = new URL(GOOGLE_ADDRESS);
            URLConnection connection = url.openConnection();
            connection.connect();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}