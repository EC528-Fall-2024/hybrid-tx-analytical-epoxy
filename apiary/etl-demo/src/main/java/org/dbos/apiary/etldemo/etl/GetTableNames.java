package org.dbos.apiary.etldemo.etl;

import org.dbos.apiary.postgres.PostgresContext;
import org.dbos.apiary.postgres.PostgresFunction;
import org.json.simple.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetTableNames extends PostgresFunction {

    private static final String getTables = "SELECT table_name FROM information_schema.tables WHERE table_type = 'BASE TABLE' AND table_schema NOT IN ('pg_catalog', 'information_schema') ORDER BY table_name;";

    public static String[] runFunction(PostgresContext ctxt) throws SQLException {
        ResultSet rs = ctxt.executeQuery(getTables);
        List<String> tableNames = new ArrayList<>();
        try {
            // Store table names in a list
            while (rs.next()) {
                tableNames.add(rs.getString("table_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Found these tables:");
        System.out.println(tableNames);
        System.out.println("Ends:");
        return tableNames.toArray(new String[0]);
    }
}