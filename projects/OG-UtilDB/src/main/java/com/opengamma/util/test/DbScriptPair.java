/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;

import com.opengamma.util.ArgumentChecker;

/**
 * Single pair of scripts.
 */
public final class DbScriptPair {

  private final String _databaseType;
  private final String _tableSet;
  private final int _version;
  private final File _createScript;
  private final File _migrateScript;

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param databaseType  the database type, not null
   * @param tableSet  the associated tables, not null
   * @param version  the version
   * @param createScript  the create script, not null
   * @param migrateScript  the migrate script, not null
   */
  DbScriptPair(String databaseType, String tableSet, int version, File createScript, File migrateScript) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    ArgumentChecker.notNull(tableSet, "tableSet");
    ArgumentChecker.notNull(createScript, "createScript");
    _databaseType = databaseType;
    _tableSet = tableSet;
    _version = version;
    _createScript = createScript;
    _migrateScript = migrateScript;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database type.
   * 
   * @return the database type, not null
   */
  public String getDatabaseType() {
    return _databaseType;
  }

  /**
   * Gets the set of associated tables.
   * 
   * @return the associated tables, not null
   */
  public String getTableSet() {
    return _tableSet;
  }

  /**
   * Gets the version of the script.
   * 
   * @return the version
   */
  public int getVersion() {
    return _version;
  }

  /**
   * Gets the SQL database creation script.
   * 
   * @return the script file, not null
   */
  public File getCreateScript() {
    return _createScript;
  }

  /**
   * Gets the SQL database migration script.
   * 
   * @return the script file, may be null
   */
  public File getMigrateScript() {
    return _migrateScript;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getDatabaseType() + ":" + getVersion() + ":" + getCreateScript() + ":" + getMigrateScript();
  }

}
