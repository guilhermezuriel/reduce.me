package com.guilhermezuriel.reduceme.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

public class Utils {

    private static final String URL_REGEX =
            "^(https?://)?" + // (http ou https)
                    "([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}" + // domain
                    "(:\\d+)?(/.*)?$"; // Port and path

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);

    public static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }


    public static String digestUrl(String url) {
        byte[] urlHashBytes;
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD-5");
            urlHashBytes = algorithm.digest(url.getBytes(StandardCharsets.UTF_8));
        }catch (NoSuchAlgorithmException e){
            urlHashBytes = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
        }
        urlHashBytes = Base64.getEncoder().encode(urlHashBytes);

        return new String(urlHashBytes).substring(0, 7);
    }

}
