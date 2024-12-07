package org.dbos.apiary.etldemo.etl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformService {
    private static final int BUFFER_SIZE = 1000;
    private static final int INITIAL_LIST_CAPACITY = 10000;

    public static List<Map<String, List<Object>>> transformColumnToRow(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            throw new IllegalArgumentException("ResultSet cannot be null");
        }

        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, List<Object>> columnToValuesMap = initializeColumnMap(metaData);
        
        processResultSet(resultSet, metaData, columnToValuesMap);

        return wrapInList(columnToValuesMap);
    }

    private static Map<String, List<Object>> initializeColumnMap(ResultSetMetaData metaData) throws SQLException {
        Map<String, List<Object>> columnToValuesMap = new HashMap<>();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            columnToValuesMap.put(
                metaData.getColumnName(i), 
                new ArrayList<>(INITIAL_LIST_CAPACITY)
            );
        }
        return columnToValuesMap;
    }

    private static void processResultSet(
            ResultSet resultSet, 
            ResultSetMetaData metaData,
            Map<String, List<Object>> columnToValuesMap) throws SQLException {
        
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> buffer = new ArrayList<>(BUFFER_SIZE);

        while (resultSet.next()) {
            Map<String, Object> row = extractRow(resultSet, metaData, columnCount);
            buffer.add(row);

            if (buffer.size() >= BUFFER_SIZE) {
                processBuffer(buffer, columnToValuesMap);
                buffer.clear();
            }
        }

        // Process any remaining rows
        if (!buffer.isEmpty()) {
            processBuffer(buffer, columnToValuesMap);
        }
    }

    private static Map<String, Object> extractRow(
            ResultSet resultSet, 
            ResultSetMetaData metaData,
            int columnCount) throws SQLException {
        
        Map<String, Object> row = new HashMap<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            row.put(metaData.getColumnName(i), resultSet.getObject(i));
        }
        return row;
    }

    private static void processBuffer(
            List<Map<String, Object>> buffer,
            Map<String, List<Object>> columnToValuesMap) {
        
        for (Map<String, Object> row : buffer) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                columnToValuesMap.get(entry.getKey()).add(entry.getValue());
            }
        }
    }

    private static List<Map<String, List<Object>>> wrapInList(
            Map<String, List<Object>> columnToValuesMap) {
        
        List<Map<String, List<Object>>> result = new ArrayList<>(1);
        result.add(columnToValuesMap);
        return result;
    }
}