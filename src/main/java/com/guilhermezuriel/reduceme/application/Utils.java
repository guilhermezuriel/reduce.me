package com.guilhermezuriel.reduceme.application;

import java.util.regex.Pattern;

public class Utils {

    private static final String URL_REGEX =
            "^(https?://)?" + // Protocolo (http ou https, opcional)
                    "([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}" + // Nome de domínio
                    "(:\\d+)?(/.*)?$"; // Porta e caminho (opcional)

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    public static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

}
