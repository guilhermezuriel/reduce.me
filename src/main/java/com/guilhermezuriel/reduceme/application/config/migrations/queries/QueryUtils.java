package com.guilhermezuriel.reduceme.application.config.migrations.queries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

public class QueryUtils {

    private static final String MIGRATION_REGEX = "^V\\d+__\\S+\\.cql$";
    public static final Pattern MIGRATION_PATTERN = Pattern.compile(MIGRATION_REGEX, Pattern.CASE_INSENSITIVE);


    public static boolean isValidMigrationPattern(String migrationPattern) {
        return MIGRATION_PATTERN.matcher(migrationPattern).matches();
    }

    /**
     * Calculates the checksum of this string.
     *
     * @param str The string to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    /* private -> for testing */
    public static long calculateChecksum(String str) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                crc32.update(line.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to calculate checksum: " +  e.getMessage());
        }

        return (long) crc32.getValue();
    }
}
