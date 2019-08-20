package registry.dataBase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

public class DataBase {

    private static class GetParameterValueWrapper implements SqlParameterSource {

        private GetParameterValue getParameterValue;

        public GetParameterValueWrapper(GetParameterValue getParameterValue) {
            this.getParameterValue = getParameterValue;
        }

        @Override
        public boolean hasValue(String paramName) {
            return this.getParameterValue != null;
        }

        @Override
        public Object getValue(String paramName) throws IllegalArgumentException {
            return getParameterValue.get(paramName);
        }
    }

    public static void getResultFromSQL(DataSource dataSource, String sql, GetParameterValue getParameterValue, GetResultSet<SqlRowSet> getResultSet) throws SQLException {

        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, new GetParameterValueWrapper(getParameterValue));
        getResultSet.get(sqlRowSet);
    }

    public static void getResultFromSQL(DataSource dataSource, String sql, GetResultSet<SqlRowSet> getResultSet) throws SQLException {
        getResultFromSQL(dataSource, sql, null, getResultSet);
    }

    public static String getJsonFromSQL(DataSource dataSource, String sql) {
        try {
            JSONArray jsonHead = new JSONArray();
            JSONArray jsonData = new JSONArray();

            getResultFromSQL(dataSource, sql, resultSet -> {
                String[] columnNames = resultSet.getMetaData().getColumnNames();
                while (resultSet.next()) {
                    JSONArray jsonRow = new JSONArray();
                    for (int i = 1; i <= columnNames.length; i++) {
                        jsonRow.put(resultSet.getObject(i));
                    }
                    jsonData.put(jsonRow);
                }
                Arrays.stream(columnNames).forEach(jsonHead::put);
            });

            return new JSONObject().put("HEAD", jsonHead).put("DATA", jsonData).toString();
        } catch (SQLException e) {
            System.out.println("Requet was not completed: \n" + e.getMessage());
        }

        return null;
    }
}
