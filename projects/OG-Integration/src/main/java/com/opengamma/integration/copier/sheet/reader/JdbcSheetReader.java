/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.reader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.opengamma.util.ArgumentChecker;

/**
 * A class to facilitate importing portfolio data from a JDBC query result.
 */
public class JdbcSheetReader extends SheetReader {
  // NOTE: This class uses JdbcTemplate rather than DbConnector
  // as such it may not work reliably across databases, notably Oracle

  private DataSource _dataSource;
  private JdbcTemplate _jdbcTemplate;
  private ResultSet _resultSet;
  private String[] _row;
  private List<Map<String, String>> _results;
  private Iterator<Map<String, String>> _iterator;
  private static final Logger s_logger = LoggerFactory.getLogger(JdbcSheetReader.class);

  public JdbcSheetReader(DataSource dataSource, String query) {
    init(new JdbcTemplate(dataSource), query);
  }

  public JdbcSheetReader(JdbcTemplate jdbcTemplate, String query) {
    init(jdbcTemplate, query);
  }

  protected void init(JdbcTemplate jdbcTemplate, String query) {
    ArgumentChecker.notNull(jdbcTemplate, "jdbcTemplate");
    ArgumentChecker.notEmpty(query, "query");

    _jdbcTemplate = jdbcTemplate;

    ResultSetExtractor<List<Map<String, String>>> extractor = new ResultSetExtractor<List<Map<String, String>>>() {
      @Override
      public List<Map<String, String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        String[] columns = new String[rs.getMetaData().getColumnCount()];
        for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
          columns[i] = rs.getMetaData().getColumnName(i + 1);
        }
        setColumns(columns);
        List<Map<String, String>> entries = new ArrayList<Map<String, String>>();
        while (rs.next()) {
          String[] rawRow = new String[rs.getMetaData().getColumnCount()];
          for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
            rawRow[i] = rs.getString(i + 1);
          }
          Map<String, String> result = new HashMap<String, String>();
          // Map read-in row onto expected columns
          for (int i = 0; i < getColumns().length; i++) {
            if (i >= rawRow.length) {
              break;
            }
            if (rawRow[i] != null && rawRow[i].trim().length() > 0) {
              result.put(getColumns()[i], rawRow[i]);
            }
          }
          entries.add(result);
        }
        return entries;
      }
    };
    _results = getJDBCTemplate().query(query, extractor);
    _iterator = _results.iterator();
  }

  private JdbcTemplate getJDBCTemplate() {
    return _jdbcTemplate;
  }

  @Override
  public Map<String, String> loadNextRow() {
    if (_iterator.hasNext()) {
      return _iterator.next();
    } else {
      return null;
    }
  }

  @Override
  public void close() {
  }
}
