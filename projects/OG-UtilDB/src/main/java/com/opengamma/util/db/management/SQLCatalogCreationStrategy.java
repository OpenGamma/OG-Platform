/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

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

  private AbstractDbManagement _dbManagement;
  private String _user;
  private String _password;
  private String _allCatalogsSql;
  private String _blankCatalog;

  /**
   * Creates an instance.
   * @param dbManagement  the dialect-specific db management class
   * @param user  the user name
   * @param password  the password
   * @param getAllCatalogsSql  the SQL to get all catalogs, not null
   * @param blankCatalog  the catalog name to create, not null 
   */
  public SQLCatalogCreationStrategy(
      AbstractDbManagement dbManagement,
      String user, 
      String password,
      String getAllCatalogsSql,
      String blankCatalog) {
    _dbManagement = dbManagement;
    _user = user;
    _password = password;
    _allCatalogsSql = getAllCatalogsSql;
    _blankCatalog = blankCatalog;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean catalogExists(String catalog) {
    @SuppressWarnings("resource")
    Connection conn = null;
    try {
      if (_user != null && !_user.equals("")) {
        conn = DriverManager.getConnection(getCatalogToConnectTo(), _user, _password);
      } else {
        // PLAT-2745, if we do not have a user, then client may be
        // attempting to login to MSSQL using integratedSecurity
        // and just the url should be sufficient
        conn = DriverManager.getConnection(getCatalogToConnectTo());
      }
      conn.setAutoCommit(true);
  
      boolean catalogAlreadyExists = false;
      try (Statement statement = conn.createStatement()) {
        try (ResultSet rs = statement.executeQuery(_allCatalogsSql)) {
          while (rs.next()) {
            String name = rs.getString("name");
            if (name.equals(catalog)) {
              catalogAlreadyExists = true;
            }
          }
        }
      }
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
      return _dbManagement.getDbHost();
    } else {
      return _dbManagement.getCatalogToConnectTo(_blankCatalog);
    }
  }

  @Override
  public void create(String catalog) {
    if (catalogExists(catalog)) {
      return; // nothing to do
    }
    
    @SuppressWarnings("resource")
    Connection conn = null;
    try {
      if (_user != null && !_user.equals("")) {
        conn = DriverManager.getConnection(getCatalogToConnectTo(), _user, _password);
      } else {
        // PLAT-2745, if we do not have a user, then client may be
        // attempting to login to MSSQL using integratedSecurity
        // and just the url should be sufficient
        conn = DriverManager.getConnection(getCatalogToConnectTo());
      }
      conn.setAutoCommit(true);
  
      String createCatalogSql = "CREATE DATABASE " + catalog;
      try (Statement statement = conn.createStatement()) {
        statement.executeUpdate(createCatalogSql);
      }
      
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
