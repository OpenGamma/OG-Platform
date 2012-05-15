/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.sheet.reader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * A class to facilitate importing portfolio data from comma-separated value files
 */
public class JdbcSheetReader extends SheetReader {

  private Connection _connection;
  private ResultSet _resultSet;
  
    
  public JdbcSheetReader(String url, String user, String password, String query) {
    
    ArgumentChecker.notEmpty(url, "url");
    ArgumentChecker.notEmpty(user, "user");
    ArgumentChecker.notEmpty(password, "password");

    // Set up JDBC connection
    try {
      _connection = DriverManager.getConnection(url, user, password);
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Could not get a JDBC connection to " + url + ": " + ex.getMessage());
    }
    
    // Perform query
    Statement statement;
    try {
      statement = _connection.createStatement();
      if (statement.execute(query) == false) {
        throw new OpenGammaRuntimeException("Query (" + query + ") returned no results");
      }
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Error executing query (" + query + "):" + ex.getMessage());
    }
    
    // Set columns
    try {
      _resultSet = statement.getResultSet();
      String[] columns = new String[_resultSet.getMetaData().getColumnCount()];
      for (int i = 0; i < _resultSet.getMetaData().getColumnCount(); i++) {
        columns[i] = _resultSet.getMetaData().getColumnName(i + 1);
      }      
      setColumns(columns);
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Could not extract column names from query result:" + ex.getMessage());
    }
  }
   
  public JdbcSheetReader(Connection connection, String query) {
    
    ArgumentChecker.notNull(connection, "connection");

    // Perform query
    Statement statement;
    try {
      statement = _connection.createStatement();
      if (statement.execute(query) == false) {
        throw new OpenGammaRuntimeException("Query (" + query + ") returned no results");
      }
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Error executing query (" + query + "):" + ex.getMessage());
    }
    
    // Set columns
    try {
      _resultSet = statement.getResultSet();
      String[] columns = new String[_resultSet.getMetaData().getColumnCount()];
      for (int i = 0; i < _resultSet.getMetaData().getColumnCount(); i++) {
        columns[i] = _resultSet.getMetaData().getColumnName(i + 1);
      }      
      setColumns(columns);
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Could not extract column names from query result:" + ex.getMessage());
    }
  }

  @Override
  public Map<String, String> loadNextRow() {
    
    Map<String, String> result = new HashMap<String, String>();

    // Read in next row and return null if EOF
    try {
      if (!_resultSet.next()) {
        return null;
      }
    
      String[] rawRow = new String[_resultSet.getMetaData().getColumnCount()];
      for (int i = 0; i < _resultSet.getMetaData().getColumnCount(); i++) {
        rawRow[i] = _resultSet.getString(i + 1);
      }
      
      // Map read-in row onto expected columns
      for (int i = 0; i < getColumns().length; i++) {
        if (i >= rawRow.length) {
          break;
        }
        if (rawRow[i] != null && rawRow[i].trim().length() > 0) {
          result.put(getColumns()[i], rawRow[i]);
        }
      }
      
    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Could not extract current row from JDBC result set:" + ex.getMessage());
    }

    return result;
  }

  @Override
  public void close() {
    try {
      _connection.close();

    } catch (SQLException ex) {
      throw new OpenGammaRuntimeException("Could not close JDBC connection: " + ex.getMessage());
    }
  }
}
