/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.IOException;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.management.DbManagement;
import com.opengamma.util.db.script.DbScript;

/**
 * Writes SQL scripts to a database.
 */
public class DbSqlScriptWriter implements SqlScriptWriter {

  private final DbManagement _dbManagement;
  private final String _catalog;
  
  /**
   * Constructs an instance.
   * 
   * @param dbManagement  the database management instance, not null
   * @param catalog  the catalog name, not null 
   */
  public DbSqlScriptWriter(DbManagement dbManagement, String catalog) {
    ArgumentChecker.notNull(dbManagement, "dbManagement");
    ArgumentChecker.notNull(catalog, "catalog");
    _dbManagement = dbManagement;
    _catalog = catalog;
  }
  
  /**
   * Gets the database management instance.
   * 
   * @return the database management instance, not null
   */
  private DbManagement getDbManagement() {
    return _dbManagement;
  }

  /**
   * Gets the catalog name.
   * 
   * @return the catalog name, not null
   */
  private String getCatalog() {
    return _catalog;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void write(String title, DbScript script) throws IOException {
    getDbManagement().executeSql(getCatalog(), null, script.getScript());
  }
  
  @Override
  public void close() throws IOException {
  }

}
