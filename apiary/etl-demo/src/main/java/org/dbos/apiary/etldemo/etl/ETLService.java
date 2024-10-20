package org.dbos.apiary.etldemo.etl;

import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

@Service
public class ETLService {

    public void runETL() {
        ResultSet resultSet = null;

        try {
            // Step 1: Extract data from PostgreSQL
            resultSet = ExtractFromPostgres.extractData();

            // Step 2: Transform data from column-based to row-based
            List<Map<String, List<Object>>> transformedData = TransformService.transformColumnToRow(resultSet);

            // Step 3: Load the transformed data into ClickHouse
            LoadToClickHouse.loadData(transformedData);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
