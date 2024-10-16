package org.dbos.apiary.etldemo.etl;

import org.springframework.stereotype.Service;

import java.sql.ResultSet;

@Service
public class ETLService {

    public void runETL() {
        ResultSet resultSet = null;

        try {
            // Step 1: Extract data from PostgreSQL
            resultSet = ExtractFromPostgres.extractData();

            // Step 2: Load the data into ClickHouse
            LoadToClickHouse.loadData(resultSet);

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
