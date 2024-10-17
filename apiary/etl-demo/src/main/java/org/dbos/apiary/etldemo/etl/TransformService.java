package org.dbos.apiary.etldemo.etl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformService {

    public static List<Map<String, List<Object>>> transformColumnToRow(ResultSet resultSet) throws Exception {
        // Create a map to store column name as key and its corresponding values as a list
        Map<String, List<Object>> columnToValuesMap = new HashMap<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Initialize the map with column names as keys and empty lists as values
        for (int i = 1; i <= columnCount; i++) {
            columnToValuesMap.put(metaData.getColumnName(i), new ArrayList<>());
        }

        // Loop through each row in the result set
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                
                // Add each row's value to the respective column list
                columnToValuesMap.get(columnName).add(columnValue);
            }
        }

        // Convert the map to a list of maps where each map represents a row in the new format
        List<Map<String, List<Object>>> rowBasedData = new ArrayList<>();

        // We convert the columnToValuesMap into a list of maps for easy processing
        for (Map.Entry<String, List<Object>> entry : columnToValuesMap.entrySet()) {
            Map<String, List<Object>> row = new HashMap<>();
            row.put(entry.getKey(), entry.getValue());
            rowBasedData.add(row);
        }

        return rowBasedData;
    }
}
