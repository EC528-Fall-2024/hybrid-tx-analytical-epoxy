package org.dbos.apiary.etldemo.etl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.SQLException;

public class TransformService {
    public static List<Map<String, List<Object>>> transformColumnToRow(ResultSet resultSet) 
            throws SQLException {
        Map<String, List<Object>> columnToValuesMap = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Initialize columns
        for (int i = 1; i <= columnCount; i++) {
            columnToValuesMap.put(metaData.getColumnName(i), new ArrayList<>());
        }

        // Transform data
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                columnToValuesMap.get(columnName).add(columnValue);
            }
        }

        // Convert to row format
        List<Map<String, List<Object>>> rowBasedData = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : columnToValuesMap.entrySet()) {
            Map<String, List<Object>> row = new HashMap<>();
            row.put(entry.getKey(), entry.getValue());
            rowBasedData.add(row);
        }

        return rowBasedData;
    }
}
