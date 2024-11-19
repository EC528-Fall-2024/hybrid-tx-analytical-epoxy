package main.java.org.dbos.apiary.etldemo.etl;

import org.dbos.apiary.postgres.PostgresFunction;
import org.dbos.apiary.postgres.PostgresContext;
import org.dbos.apiary.function.FunctionOutput;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

// Add these imports
import org.dbos.apiary.etldemo.etl.TransformService;
import org.dbos.apiary.etldemo.etl.LoadToClickHouse;

public class PerformETL extends PostgresFunction {
    private static final String EXTRACT_QUERY = 
        "SELECT * FROM %s";

    public static FunctionOutput runFunction(PostgresContext ctxt, String tableName,
            String clickhouseUrl, String clickhouseUser, String clickhousePassword) 
            throws SQLException {
        // Extract data from PostgreSQL
        ResultSet rs = ctxt.executeQuery(String.format(EXTRACT_QUERY, tableName));
        
        // Transform data
        List<Map<String, List<Object>>> transformedData = 
            TransformService.transformColumnToRow(rs);
        
        // Load to ClickHouse
        LoadToClickHouse.loadData(transformedData, tableName, 
            clickhouseUrl, clickhouseUser, clickhousePassword);
        
        return new FunctionOutput("ETL completed for table: " + tableName);
    }
}