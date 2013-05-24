/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.util.LinkedList;
import java.util.List;


/**
 * Represents the available metadata for a database schema group.
 */
public class DbSchemaGroupMetadata {
  
  private static final String CREATE_TYPE = "create";
  private static final String MIGRATE_TYPE = "migrate";

  private final String _schemaGroupName;
  private final String _baseResourcePath;
  private final int _currentVersion;
  
  public DbSchemaGroupMetadata(String schemaGroupName, String baseResourcePath, int currentVersion) {
    _schemaGroupName = schemaGroupName;
    _baseResourcePath = baseResourcePath;
    _currentVersion = currentVersion;
  }

  /**
   * Gets the schema group name.
   * 
   * @return the schema group name, not null
   */
  public String getSchemaGroupName() {
    return _schemaGroupName;
  }

  /**
   * Gets the current schema group version.
   * 
   * @return the current version
   */
  public int getCurrentVersion() {
    return _currentVersion;
  }

  /**
   * Gets the SQL for migrating from a particular version to the next version.
   *  
   * @param dbVendorName  the database vendor name, not null
   * @param fromVersion  the version to migrate from
   * @return the SQL, or null if not found for vendor or version
   */
  public DbScript getMigrateScript(String dbVendorName, int fromVersion) {
    return getScript(dbVendorName, MIGRATE_TYPE, fromVersion + 1);
  }
  
  public List<DbScript> getMigrateScripts(String dbVendorName, int fromVersion) {
    List<DbScript> result = new LinkedList<DbScript>();
    for (int i = fromVersion; i < getCurrentVersion(); i++) {
      result.add(getMigrateScript(dbVendorName, i));
    }
    return result;
  }
  
  /**
   * Gets the SQL for creating the database objects for this schema group at the current version.
   * 
   * @param dbVendorName  the database vendor name, not null
   * @return the SQL, null if not found for vendor
   */
  public DbScript getCreateScript(String dbVendorName) {
    return getCreateScript(dbVendorName, getCurrentVersion());
  }
  
  /**
   * Gets the SQL for creating the database objects for this schema group at a particular version.
   * 
   * @param dbVendorName  the database vendor name, not null
   * @param version  the version
   * @return the SQL, null if not found for vendor or version
   */
  public DbScript getCreateScript(String dbVendorName, int version) {
    return getScript(dbVendorName, CREATE_TYPE, version);
  }

  //-------------------------------------------------------------------------
  private DbScript getScript(String dbVendorName, String scriptType, int version) {
    String sqlResourcePath = getSqlScriptPath(dbVendorName, scriptType, version);
    return new ClasspathDbScript(sqlResourcePath);
  }
  
  private String getBaseResourcePath() {
    return _baseResourcePath;
  }
  
  private String getSqlScriptPath(String dbVendor, String scriptType, int version) {
    String fileName = "V_" + version + "__" + scriptType + "_" + getSchemaGroupName() + ".sql";
    return getBaseResourcePath() + "/" + scriptType + "/" + dbVendor + "/" + getSchemaGroupName() + "/" + fileName;
  }
  
}
