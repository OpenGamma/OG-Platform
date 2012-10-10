/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract representation of a database operation on master database schemas using the database installation scripts.
 * 
 * @param <T>  the type of database tool context
 */
public abstract class AbstractDbSchemaScriptOperation<T extends DbToolContext> extends AbstractDbOperation<T> {

  private static final String CREATE_PATH = "create";
  private static final String MIGRATE_PATH = "migrate";
  private static final String SCHEMA_GROUP_PATH_SUFFIX = "_db";
  
  private static final Pattern CREATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__create_(.+?)\\.sql");
  private static final Pattern MIGRATE_SCRIPT_PATTERN = Pattern.compile("^V_([0-9]+)__(.+?)\\.sql");
  
  protected AbstractDbSchemaScriptOperation(T dbToolContext, boolean write, File outputFile) {
    super(dbToolContext, write, outputFile);
    contextNotNull(getDbToolContext().scriptsResource());
    contextNotNull(getDbToolContext().schemaGroups());
    contextNotNull(getDbToolContext().dbManagement());
  }
  
  protected File getLatestSchemaCreateScript(String schemaGroup) {
    File schemaGroupCreateBaseDir = getScriptsDir(CREATE_PATH, schemaGroup);
    int latestVersion = -1;
    File latestCreateScript = null;
    for (File file : schemaGroupCreateBaseDir.listFiles()) {
      Matcher m = CREATE_SCRIPT_PATTERN.matcher(file.getName());
      if (!m.matches()) {
        continue;
      }
      int version = Integer.parseInt(m.group(1));
      if (version > latestVersion) {
        latestCreateScript = file;
      }
    }
    if (latestCreateScript == null) {
      throw new OpenGammaRuntimeException("No create scripts found for schema group '" + schemaGroup + "'");
    }
    return latestCreateScript;
  }
  
  /**
   * Gets an ordered list of migration scripts to upgrade a schema group from the current to the latest version.
   * @param schemaGroup  the schema group name, not null
   * @return an ordered list of migration scripts, empty if nothing to upgrade, null if versioning not supported
   */
  protected List<File> getSchemaMigrateScripts(String schemaGroup) {
    ArgumentChecker.notNull(schemaGroup, "schemaGroup");
    Integer currentSchemaVersion = getCurrentSchemaVersion(schemaGroup);
    if (currentSchemaVersion == null) {
      return null;
    }
    File schemaGroupMigrateBaseDir = getScriptsDir(MIGRATE_PATH, schemaGroup);
    Map<Integer, File> versionedFiles = new TreeMap<Integer, File>();
    for (File file : schemaGroupMigrateBaseDir.listFiles()) {
      Matcher m = MIGRATE_SCRIPT_PATTERN.matcher(file.getName());
      if (!m.matches()) {
        continue;
      }
      int version = Integer.parseInt(m.group(1));
      if (version > currentSchemaVersion) {
        versionedFiles.put(version, file);
      }
    }
    return new ArrayList<File>(versionedFiles.values());
  }
  
  private Integer getCurrentSchemaVersion(String schemaGroup) {
    return getDbToolContext().getDbManagement().getSchemaGroupVersion(getDbToolContext().getCatalog(), null, schemaGroup);
  }

  private File getScriptsDir(String scriptType, String schemaGroup) {
    File scriptsBaseDir;
    try {
      scriptsBaseDir = getDbToolContext().getScriptsResource().getFile();
    } catch (IOException e) {
      // TODO jonathan 2012-10-09 -- support classpath resources contained in JARs
      throw new OpenGammaRuntimeException("Error resolving scripts base directory on file system", e);
    }
    if (!scriptsBaseDir.exists() || !scriptsBaseDir.isDirectory()) {
      throw new OpenGammaRuntimeException("Schema scripts base directory not found at: " + scriptsBaseDir);
    }
    File scriptTypeBaseDir = new File(scriptsBaseDir, scriptType);
    if (!scriptTypeBaseDir.exists() || !scriptTypeBaseDir.isDirectory()) {
      throw new OpenGammaRuntimeException("'" + scriptType + "' scripts directory not found at " + scriptTypeBaseDir);
    }
    String databaseName = getDbToolContext().getDbManagement().getDatabaseName();
    File databaseBaseDir = new File(scriptTypeBaseDir, databaseName);
    if (!databaseBaseDir.exists() || !databaseBaseDir.isDirectory()) {
      throw new OpenGammaRuntimeException("Unknown database '" + databaseName + "' for '" + scriptType + "' scripts; directory not found at " + databaseBaseDir);
    }
    File schemaGroupBaseDir = new File(databaseBaseDir, schemaGroup + SCHEMA_GROUP_PATH_SUFFIX);
    if (!schemaGroupBaseDir.exists() || !schemaGroupBaseDir.isDirectory()) {
      throw new OpenGammaRuntimeException("Unknown schema group '" + schemaGroup + "' for database '" + databaseName + "'; scripts directory not found at " + schemaGroupBaseDir);
    }
    return schemaGroupBaseDir;
  }

}
