/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.opengamma.OpenGammaRuntimeException;

/**
 * A database catalog creation strategy implementation that uses SQL.
 */
public class SQLCatalogCreationStrategy implements CatalogCreationStrategy {

  private String _dbServerHost;
  private String _user;
  private String _password;
  private String _allCatalogsSql;
  private String _blankCatalog;

  /**
   * Creates an instance.
   * @param dbServerHost  the database server, not null
   * @param user  the user name
   * @param password  the password
   * @param getAllCatalogsSql  the SQL to get all catalogs, not null
   * @param blankCatalog  the catalog name to create, not null 
   */
  public SQLCatalogCreationStrategy(
      String dbServerHost, 
      String user, 
      String password,
      String getAllCatalogsSql,
      String blankCatalog) {
    _dbServerHost = dbServerHost;
    _user = user;
    _password = password;
    _allCatalogsSql = getAllCatalogsSql;
    _blankCatalog = blankCatalog;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean catalogExists(String catalog) {
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(getCatalogToConnectTo(), _user, _password);
      conn.setAutoCommit(true);
  
      Statement statement = conn.createStatement();
      ResultSet rs = statement.executeQuery(_allCatalogsSql);
      
      boolean catalogAlreadyExists = false;
      while (rs.next()) {
        String name = rs.getString("name");
        if (name.equals(catalog)) {
          catalogAlreadyExists = true;
        }
      }
      
      rs.close();
      statement.close();
      
      return catalogAlreadyExists;
      
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to create catalog", e);     
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
        }
      }
    } 
  }

  private String getCatalogToConnectTo() {
    if (_blankCatalog == null) {
      return _dbServerHost;
    } else {
      return _dbServerHost + "/" + _blankCatalog;
    }
  }

  @Override
  public void create(String catalog) {
    if (catalogExists(catalog)) {
      return; // nothing to do
    }
    
    Connection conn = null;
    try {
      conn = DriverManager.getConnection(getCatalogToConnectTo(), 
          _user, _password);
      conn.setAutoCommit(true);
  
      String createCatalogSql = "CREATE DATABASE " + catalog;
      Statement statement = conn.createStatement();
      statement.executeUpdate(createCatalogSql);
      statement.close();
  
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to create catalog", e);      
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
        }
      }
    }
  }

}
