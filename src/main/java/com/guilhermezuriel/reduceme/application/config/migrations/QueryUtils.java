package com.guilhermezuriel.reduceme.application.config.migrations;

public class QueryUtils {

    public static String addColumn(String table, String column, String dataType) {
        var builder = new StringBuilder();
        builder.append("ALTER TABLE ").append(table).append("\n").append("ADD").append(" ").append(column).append(" ").append(dataType);
        return builder.toString();
    }
}
