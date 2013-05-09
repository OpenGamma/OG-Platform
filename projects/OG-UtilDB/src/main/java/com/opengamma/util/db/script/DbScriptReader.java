/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.lambdava.tuple.Pair;

/**
 * Provides access to database scripts from a standard directory structure.
 * <p>
 * The directory structure is /db/[script-type]/[database-name]/[group-name]/, where:
 * <ul>
 * <li> script-type is 'create' or 'migrate'
 * <li> database-name is an internal name for the database vendor
 * <li> group-name describes a group of database objects, normally corresponds to a OpenGamma Master 
 * </ul>
 */
public class DbScriptReader {
  
  /**
   * The create script directory name.
   */
  private static final String CREATE_PATH = "create";
  /**
   * The migrate script directory name.
   */
  private static final String MIGRATE_PATH = "migrate";
  /**
   * The schema name directory suffix
   */
  private static final String SCHEMA_PATH_SUFFIX = "_db";
  /**
   * The create script name pattern.
   */
  private static final Pattern CREATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__create_(.+?)\\.sql");
  /**
   * The migrate script name pattern.
   */
  private static final Pattern MIGRATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__(.+?)\\.sql");
  /**
   * The schema directory name pattern.
   */
  private static final Pattern SCHEMA_DIR_PATTERN = Pattern.compile("^(.+)" + SCHEMA_PATH_SUFFIX);
  
  /**
   * The base directory.
   */
  private final DbScriptDirectory _baseDir;
  
  /**
   * Creates an instance.
   * 
   * @param baseDir  the base directory, not null
   */
  public DbScriptReader(DbScriptDirectory baseDir) {
    ArgumentChecker.notNull(baseDir, "baseDir");
    _baseDir = baseDir;
  }
  
  //-------------------------------------------------------------------------
  public DbScriptDirectory getBaseDir() {
    return _baseDir;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets an ordered list of migration scripts required to upgrade a schema from the current to the latest version.
   * 
   * @param databaseName  the internal database vendor name, not null
   * @param schemaName  the schema name, not null
   * @param currentVersion  the current database version from which to migrate
   * @return an ordered list of migration scripts, empty if nothing to upgrade
   */
  public List<DbScript> getMigrationScripts(String databaseName, String schemaName, int currentVersion) {
    ArgumentChecker.notNull(databaseName, "databaseName");
    ArgumentChecker.notNull(schemaName, "schemaName");
    DbScriptDirectory schemaMigrateBaseDir = getScriptsDir(MIGRATE_PATH, databaseName, schemaName);
    Map<Integer, DbScript> versionedScripts = new TreeMap<Integer, DbScript>();
    for (DbScript script : schemaMigrateBaseDir.getScripts()) {
      Matcher m = MIGRATE_SCRIPT_PATTERN.matcher(script.getName());
      if (!m.matches()) {
        continue;
      }
      int version = Integer.parseInt(m.group(1));
      if (version > currentVersion) {
        versionedScripts.put(version, script);
      }
    }
    return new ArrayList<DbScript>(versionedScripts.values());
  }

  /**
   * Gets the creation script for the latest version of a given schema.
   * 
   * @param databaseName  the internal database vendor name, not null
   * @param schemaName  the schema name, not null
   * @return the creation script, not null
   */
  public DbScript getCreationScript(String databaseName, String schemaName) {
    return getLatestCreationScript(databaseName, schemaName).getFirst();
  }
  
  /**
   * Gets the version number of the latest creation script for a database vendor and schema.
   * 
   * @param databaseName  the internal database vendor name, not null
   * @param schemaName  the schema name, not null
   * @return the version number of the latest creation script
   */
  public int getLatestCreationScriptVersion(String databaseName, String schemaName) {
    return getLatestCreationScript(databaseName, schemaName).getSecond();
  }
  
  private Pair<DbScript, Integer> getLatestCreationScript(String databaseName, String schemaName) {
    ArgumentChecker.notNull(databaseName, "databaseName");
    ArgumentChecker.notNull(schemaName, "schemaName");
    DbScriptDirectory schemaCreateBaseDir = getScriptsDir(CREATE_PATH, databaseName, schemaName);
    int latestVersion = -1;
    DbScript latestCreateScript = null;
    for (DbScript script : schemaCreateBaseDir.getScripts()) {
      Matcher m = CREATE_SCRIPT_PATTERN.matcher(script.getName());
      if (!m.matches()) {
        continue;
      }
      int version = Integer.parseInt(m.group(1));
      if (version > latestVersion) {
        latestCreateScript = script;
        latestVersion = version;
      }
    }
    if (latestCreateScript == null) {
      throw new OpenGammaRuntimeException("No create scripts found for schema '" + schemaName + "'");
    }
    return Pair.of(latestCreateScript, latestVersion);    
  }
  
  /**
   * Gets all database vendor names.
   * 
   * @return a set of all database vendor names, not null
   */
  public Set<String> getDatabaseVendors() {
    Set<String> databaseNames = new HashSet<String>();
    for (DbScriptDirectory directory : getDatabaseVendorsDir(CREATE_PATH).getSubdirectories()) {
      databaseNames.add(directory.getName());
    }
    return databaseNames;
  }
  
  /**
   * Gets the schema names for a given database vendor.
   *
   * @param databaseName  the internal database vendor name, not null
   * @return the schema names, not null
   */
  public Set<String> getAllSchemaNames(String databaseName) {
    Set<String> schemaNames = new HashSet<String>();
    DbScriptDirectory schemasDir = getSchemasDir(CREATE_PATH, databaseName);
    for (DbScriptDirectory schemaDir : schemasDir.getSubdirectories()) {
      Matcher m = SCHEMA_DIR_PATTERN.matcher(schemaDir.getName());
      if (!m.matches()) {
        continue;
      }
      schemaNames.add(m.group(1));
    }
    return schemaNames;
  }
  
  /**
   * Gets a map of schema names to the latest schema version.
   * 
   * @return a map from schema name to the latest schema version, not null
   */
  public Map<String, Integer> getLatestVersions() {
    Map<String, Integer> result = new HashMap<>();
    for (String databaseName : getDatabaseVendors()) {
      for (String schemaName : getAllSchemaNames(databaseName)) {
        int latestVersion = getLatestCreationScriptVersion(databaseName, schemaName);
        Integer existingLatest = result.get(schemaName);
        // Use the latest version across all database vendors - it's feasible that some schema upgrades are added first
        if (existingLatest == null || existingLatest < latestVersion) {
          result.put(schemaName, latestVersion);
        }
      }
    }
    return result;
  }
  
  //-------------------------------------------------------------------------
  private DbScriptDirectory getDatabaseVendorsDir(String scriptType) {
    return getBaseDir().getSubdirectory(scriptType);
  }
  
  private DbScriptDirectory getSchemasDir(String scriptType, String databaseName) {
    return getDatabaseVendorsDir(scriptType).getSubdirectory(databaseName);
  }
  
  private DbScriptDirectory getScriptsDir(String scriptType, String databaseName, String schemaName) {
    return getSchemasDir(scriptType, databaseName).getSubdirectory(schemaName + SCHEMA_PATH_SUFFIX);
  }

}
