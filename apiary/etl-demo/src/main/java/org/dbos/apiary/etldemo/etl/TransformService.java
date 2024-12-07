package org.dbos.apiary.etldemo.etl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformService {
    // private static final int BUFFER_SIZE = 10000;

    public static List<Map<String, List<Object>>> transformColumnToRow(ResultSet resultSet) throws Exception {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        Map<String, List<Object>> columnToValuesMap = new HashMap<>();
        
        // Initialize column lists
        for (int i = 1; i <= columnCount; i++) {
            columnToValuesMap.put(metaData.getColumnName(i), new ArrayList<>());
        }
    
        final int BUFFER_SIZE = 1000;
        List<Map<String, Object>> buffer = new ArrayList<>(BUFFER_SIZE);
    
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            buffer.add(row);
    
            if (buffer.size() >= BUFFER_SIZE) {
                processBuffer(buffer, columnToValuesMap);
                buffer.clear();
            }
        }
    
        // Process remaining rows
        if (!buffer.isEmpty()) {
            processBuffer(buffer, columnToValuesMap);
        }
    
        List<Map<String, List<Object>>> result = new ArrayList<>();
        result.add(columnToValuesMap);
        return result;
    }
    
    private static void processBuffer(List<Map<String, Object>> buffer, 
                                    Map<String, List<Object>> columnToValuesMap) {
        for (Map<String, Object> row : buffer) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                columnToValuesMap.get(entry.getKey()).add(entry.getValue());
            }
        }
    }

    private static List<Map<String, List<Object>>> convertToFinalFormat(Map<String, List<Object>> columnToValuesMap) {
        List<Map<String, List<Object>>> rowBasedData = new ArrayList<>();
        for (Map.Entry<String, List<Object>> entry : columnToValuesMap.entrySet()) {
            Map<String, List<Object>> row = new HashMap<>();
            row.put(entry.getKey(), entry.getValue());
            rowBasedData.add(row);
        }
        return rowBasedData;
    }
}