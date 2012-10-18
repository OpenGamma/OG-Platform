/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

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
   * The group name directory suffix
   */
  private static final String GROUP_PATH_SUFFIX = "_db";
  /**
   * The create script name pattern.
   */
  private static final Pattern CREATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__create_(.+?)\\.sql");
  /**
   * The migrate script name pattern.
   */
  private static final Pattern MIGRATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__(.+?)\\.sql");
  /**
   * The group directory name pattern.
   */
  private static final Pattern GROUP_DIR_PATTERN = Pattern.compile("^(.+)" + GROUP_PATH_SUFFIX);
  
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
   * Gets an ordered list of migration scripts to upgrade a schema group from the current to the latest version.
   * 
   * @param databaseName  the internal database vendor name, not null
   * @param groupName  the group name of the database objects, not null
   * @param currentVersion  the current database version from which to migrate
   * @return an ordered list of migration scripts, empty if nothing to upgrade
   */
  public List<DbScript> getMigrationScripts(String databaseName, String groupName, int currentVersion) {
    ArgumentChecker.notNull(databaseName, "databaseName");
    ArgumentChecker.notNull(groupName, "groupName");
    DbScriptDirectory groupMigrateBaseDir = getScriptsDir(MIGRATE_PATH, databaseName, groupName);
    Map<Integer, DbScript> versionedScripts = new TreeMap<Integer, DbScript>();
    for (DbScript script : groupMigrateBaseDir.getScripts()) {
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
   * Gets the creation script for the latest version of a given group.
   * 
   * @param databaseName  the internal database vendor name, not null
   * @param groupName  the group name of the database objects, not null
   * @return the creation script, not null
   */
  public DbScript getCreationScript(String databaseName, String groupName) {
    ArgumentChecker.notNull(databaseName, "databaseName");
    ArgumentChecker.notNull(groupName, "groupName");
    DbScriptDirectory groupCreateBaseDir = getScriptsDir(CREATE_PATH, databaseName, groupName);
    int latestVersion = -1;
    DbScript latestCreateScript = null;
    for (DbScript script : groupCreateBaseDir.getScripts()) {
      Matcher m = CREATE_SCRIPT_PATTERN.matcher(script.getName());
      if (!m.matches()) {
        continue;
      }
      int version = Integer.parseInt(m.group(1));
      if (version > latestVersion) {
        latestCreateScript = script;
      }
    }
    if (latestCreateScript == null) {
      throw new OpenGammaRuntimeException("No create scripts found for group '" + groupName + "'");
    }
    return latestCreateScript;
  }
  
  /**
   * Gets the group names.
   *
   * @param databaseName  the internal database vendor name, not null
   * @return the group names, not null
   */
  public Set<String> getAllGroupNames(String databaseName) {
    Set<String> groupNames = new HashSet<String>();
    DbScriptDirectory schemaGroupsDir = getSchemaGroupsDir(CREATE_PATH, databaseName);
    for (DbScriptDirectory groupDir : schemaGroupsDir.getSubdirectories()) {
      Matcher m = GROUP_DIR_PATTERN.matcher(groupDir.getName());
      if (!m.matches()) {
        continue;
      }
      groupNames.add(m.group(1));
    }
    return groupNames;
  }
  
  //-------------------------------------------------------------------------
  private DbScriptDirectory getSchemaGroupsDir(String scriptType, String databaseName) {
    return getBaseDir().getSubdirectory(scriptType).getSubdirectory(databaseName);
  }
  
  private DbScriptDirectory getScriptsDir(String scriptType, String databaseName, String schemaGroup) {
    return getSchemaGroupsDir(scriptType, databaseName).getSubdirectory(schemaGroup + GROUP_PATH_SUFFIX);
  }

}
