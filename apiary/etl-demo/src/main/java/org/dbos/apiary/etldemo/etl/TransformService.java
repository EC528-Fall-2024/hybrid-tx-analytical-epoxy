package org.dbos.apiary.etldemo.etl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformService {
    private static final int BUFFER_SIZE = 10000;

    public static List<Map<String, List<Object>>> transformColumnToRow(ResultSet resultSet) throws Exception {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        // Use ConcurrentHashMap for thread-safe operations
        Map<String, List<Object>> columnToValuesMap = new ConcurrentHashMap<>();
        
        // Initialize column lists with capacity
        for (int i = 1; i <= columnCount; i++) {
            columnToValuesMap.put(metaData.getColumnName(i), 
                                new ArrayList<>(BUFFER_SIZE));
        }

        // Process in batches
        Object[] buffer = new Object[BUFFER_SIZE];
        int count = 0;

        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            buffer[count++] = row;

            if (count == BUFFER_SIZE) {
                processBuffer(buffer, count, columnToValuesMap);
                count = 0;
            }
        }

        // Process remaining rows
        if (count > 0) {
            processBuffer(buffer, count, columnToValuesMap);
        }

        // Convert to final format
        return convertToFinalFormat(columnToValuesMap);
    }

    private static void processBuffer(Object[] buffer, int count,
                                    Map<String, List<Object>> columnToValuesMap) {
        for (int i = 0; i < count; i++) {
            @SuppressWarnings("unchecked")
            Map<String, Object> row = (Map<String, Object>) buffer[i];
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                columnToValuesMap.get(entry.getKey()).add(entry.getValue());
            }
        }
    }

    private static List<Map<String, List<Object>>> convertToFinalFormat(
            Map<String, List<Object>> columnToValuesMap) {
        List<Map<String, List<Object>>> rowBasedData = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : columnToValuesMap.entrySet()) {
            Map<String, List<Object>> row = new HashMap<>();
            row.put(entry.getKey(), entry.getValue());
            rowBasedData.add(row);
        }
        return rowBasedData;
    }
}