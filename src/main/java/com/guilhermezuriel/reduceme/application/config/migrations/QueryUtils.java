package com.guilhermezuriel.reduceme.application.config.migrations;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.CRC32;

public class QueryUtils {

    public static String addColumn(String table, String column, String dataType) {
        var builder = new StringBuilder();
        builder.append("ALTER TABLE ").append(table).append("\n").append("ADD").append(" ").append(column).append(" ").append(dataType);
        return builder.toString();
    }

    public static String registerMigration(String table, String column, String dataType) {
        var builder = new StringBuilder();
        builder.append("ALTER TABLE cms_cql_migration_system").append("\n").append("ADD").append(" ").append(column).append(" ").append(dataType);
        return builder.toString();
    }

    /**
     * Calculates the checksum of this string.
     *
     * @param str The string to calculate the checksum for.
     * @return The crc-32 checksum of the bytes.
     */
    /* private -> for testing */
    static int calculateChecksum(String str) {
        final CRC32 crc32 = new CRC32();

        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                crc32.update(line.getBytes("UTF-8"));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to calculate checksum: " +  e.getMessage());
        }

        return (int) crc32.getValue();
    }
}
