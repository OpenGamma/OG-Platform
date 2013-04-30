/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.difference;
import static com.opengamma.util.RegexUtils.extract;
import static com.opengamma.util.RegexUtils.matches;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ZipUtils;

/**
 * Manager of database scripts.
 */
public final class DbScripts {

  /** Script files by relative location. */
  private static final File SCRIPT_RELATIVE_PATH = new File(DbTool.getWorkingDirectory(), "../OG-MasterDB");
  /** Script files using zips built by Ant. */
  private static final File SCRIPT_ZIP_PATH = new File(DbTool.getWorkingDirectory(), "lib/sql/com.opengamma/og-masterdb");
  /** Script expansion. */
  private static final File SCRIPT_INSTALL_DIR = new File(DbTool.getWorkingDirectory(), "temp/" + DbScripts.class.getSimpleName());
  /** The script directory. */
  private static volatile File s_scriptDir;

  private static final String DATABASE_INSTALL_FOLDER = "install/db";
  private static final Pattern DATABASE_SCRIPT_FOLDER_PATTERN = Pattern.compile("(.+)_db");
  private static final String DATABASE_CREATE_FOLDER = DATABASE_INSTALL_FOLDER + File.separatorChar + "create";
  private static final String DATABASE_MIGRATE_FOLDER = DATABASE_INSTALL_FOLDER + File.separatorChar + "migrate";
  private static final String DATABASE_CREATE_SCRIPT_PREFIX = "V_";
  private static final String DATABASE_MIGRATE_SCRIPT_PREFIX = "V_";
  private static final Pattern CREATE_SCRIPT_PATTERN = Pattern.compile("^" + DATABASE_CREATE_SCRIPT_PREFIX + "([0-9]+)__create_(.+?)\\.sql");
  private static final Pattern MIGRATE_SCRIPT_PATTERN = Pattern.compile("^" + DATABASE_MIGRATE_SCRIPT_PREFIX + "([0-9]+)__(.+?)\\.sql");

  /** The base script directories. */
  private final Collection<File> _scriptDirs;
  /** The database type. */
  private final String _databaseType;
  /** The scripts, by database type and version. */
  private final Map<String, SortedMap<Integer, DbScriptPair>> _scripts;

  //-------------------------------------------------------------------------
  /**
   * Gets the initialized the script directory.
   * 
   * @return the script directory, not null
   */
  public static File getSqlScriptDir() {
    if (s_scriptDir == null) {
      synchronized (DbScripts.class) {
        if (s_scriptDir == null) {
          s_scriptDir = createSQLScripts();
        }
      }
    }
    return s_scriptDir;
  }

  private static File createSQLScripts() {
    if (SCRIPT_RELATIVE_PATH.exists()) {
      return SCRIPT_RELATIVE_PATH.getAbsoluteFile();
    }
    if (SCRIPT_ZIP_PATH.exists()) {
      if (SCRIPT_INSTALL_DIR.exists()) {
        FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
      }
      for (File file : (Collection<File>) FileUtils.listFiles(SCRIPT_ZIP_PATH, new String[] {"zip"}, false)) {
        try {
          ZipUtils.unzipArchive(file, SCRIPT_INSTALL_DIR);
        } catch (IOException ex) {
          throw new OpenGammaRuntimeException("Unable to unzip database scripts: " + SCRIPT_INSTALL_DIR);
        }
      }
      return SCRIPT_INSTALL_DIR.getAbsoluteFile();
    }
    throw new IllegalArgumentException("Unable to find database scripts. Tried: " + SCRIPT_RELATIVE_PATH + " and " + SCRIPT_ZIP_PATH);
  }

  /**
   * Deletes the script directory.
   */
  public static void deleteSqlScriptDir() {
    FileUtils.deleteQuietly(SCRIPT_INSTALL_DIR);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scripts.
   * 
   * @param scriptDirs  the script directories, not null
   * @param databaseType  the database type, not null
   * @return the scripts, not null
   * @throws IllegalArgumentException if the scripts do not exist
   */
  public static DbScripts of(Collection<File> scriptDirs, String databaseType) {
    ArgumentChecker.notNull(databaseType, "databaseType");
    return new DbScripts(scriptDirs, databaseType);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param databaseType  the database type, not null
   * @param version  the version
   * @param createScript  the create script, not null
   * @param migrateScript  the migrate script, not null
   */
  private DbScripts(Collection<File> scriptDirs, String databaseType) {
    ArgumentChecker.notNull(scriptDirs, "scriptDirs");
    ArgumentChecker.notNull(databaseType, "databaseType");
    _scriptDirs = scriptDirs;
    _databaseType = databaseType;
    _scripts = buildMap();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the SQL database creation script.
   * 
   * @return the script file, not null
   */
  public Collection<File> getScriptDirs() {
    return _scriptDirs;
  }

  /**
   * Gets the database type.
   * 
   * @return the database type, not null
   */
  public String getDatabaseType() {
    return _databaseType;
  }

  /**
   * Gets the script pairs.
   * Keyed by table set and version.
   * 
   * @return the map of scripts, not null
   */
  public Map<String, SortedMap<Integer, DbScriptPair>> getScriptPairs() {
    return _scripts;
  }

  /**
   * Gets the latest script pair for a schema.
   * Keyed by schema and version.
   * 
   * @param tableSet  the table set, not null
   * @return the script pair, not null
   */
  public DbScriptPair getLatestScriptPair(String tableSet) {
    SortedMap<Integer, DbScriptPair> versions = _scripts.get(tableSet);
    if (versions == null) {
      throw new IllegalArgumentException("Unknown table set");
    }
    return versions.get(versions.lastKey());
  }

  //-------------------------------------------------------------------------
  /**
   * @return schema => version_number => (create_script, migrate_script)
   */
  private Map<String, SortedMap<Integer, DbScriptPair>> buildMap() {
    ConcurrentMap<String, ConcurrentMap<Integer, File>> createScripts = new ConcurrentHashMap<String, ConcurrentMap<Integer, File>>();
    ConcurrentMap<String, ConcurrentMap<Integer, File>> migrateScripts = new ConcurrentHashMap<String, ConcurrentMap<Integer, File>>();
    
    // for each script directory, find the files
    for (File scriptDir : _scriptDirs) {
      findScriptFiles(scriptDir, createScripts, DATABASE_CREATE_FOLDER, CREATE_SCRIPT_PATTERN);
      findScriptFiles(scriptDir, migrateScripts, DATABASE_MIGRATE_FOLDER, MIGRATE_SCRIPT_PATTERN);
    }
    
    // ensure valid script file setup
    Set<String> createTableSets = createScripts.keySet();
    Set<String> migrateTableSets = migrateScripts.keySet();
    Set<String> unmatchedCreateDbDirs = difference(migrateTableSets, createTableSets);
    if (unmatchedCreateDbDirs.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      for (String unmatchedCreateDbDir : unmatchedCreateDbDirs) {
        errorMessage.append("There is no corresponding create db directory for migrate one: " + unmatchedCreateDbDir + "\n");
      }
      throw new OpenGammaRuntimeException(errorMessage.toString());
    }
    Set<String> unmatchedMigrateDbDirs = difference(createTableSets, migrateTableSets);
    if (unmatchedMigrateDbDirs.size() > 0) {
      StringBuilder errorMessage = new StringBuilder();
      for (String unmatchedMigrateDbDir : unmatchedMigrateDbDirs) {
        errorMessage.append("There is no corresponding migrate db directory for create one: " + unmatchedMigrateDbDir + "\n");
      }
      throw new OpenGammaRuntimeException(errorMessage.toString());
    }
    
    // build output
    final ConcurrentMap<String, SortedMap<Integer, DbScriptPair>> scripts = new ConcurrentHashMap<>();
    for (String tableSet : migrateTableSets) {
      ConcurrentMap<Integer, File> versionedCreateScripts = createScripts.get(tableSet);
      ConcurrentMap<Integer, File> versionedMigrateScripts = migrateScripts.get(tableSet);
      Set<Integer> versions = versionedCreateScripts.keySet();
      for (Integer version : versions) {
        File createScript = versionedCreateScripts.get(version);
        File migrateScript = versionedMigrateScripts.get(version);
        scripts.putIfAbsent(tableSet, new TreeMap<Integer, DbScriptPair>());
        scripts.get(tableSet).put(version, new DbScriptPair(_databaseType, tableSet, version, createScript, migrateScript));
      }
    }
    
    if (scripts.isEmpty()) {
      throw new OpenGammaRuntimeException("No script directories found: " + _scriptDirs);
    }
    return scripts;
  }

  private void findScriptFiles(File scriptDir, ConcurrentMap<String, ConcurrentMap<Integer, File>> scriptMap, String subDir, Pattern pattern) {
    // find the files
    File dbCreateSuperDir = new File(scriptDir, subDir + File.separatorChar + _databaseType);
    Map<File, Map<Integer, File>> scriptsFiles = findScriptFiles(dbCreateSuperDir, pattern);
    // build the map
    for (Map.Entry<File, Map<Integer, File>> dbFolder2versionedScripts : scriptsFiles.entrySet()) {
      File dbFolder = dbFolder2versionedScripts.getKey();
      scriptMap.putIfAbsent(dbFolder.getName(), new ConcurrentHashMap<Integer, File>());
      Map<Integer, File> versionedScripts = dbFolder2versionedScripts.getValue();
      for (Map.Entry<Integer, File> version2script : versionedScripts.entrySet()) {
        Integer version = version2script.getKey();
        File script = version2script.getValue();
        
        ConcurrentMap<Integer, File> dbScripts = scriptMap.get(dbFolder.getName());
        File prev = dbScripts.putIfAbsent(version, script.getAbsoluteFile());
        if (prev != null) {
          throw new OpenGammaRuntimeException("Can't add " + script.getAbsolutePath() + " script. Version " + version + " already added by " + prev.getAbsolutePath() + " script");
        }
      }
    }
  }

  /**
   * Creates map from versions to sql scripts.
   * 
   * @param dbCreateSuperDir  the directory holding versioned scripts, not null
   * @param scriptPattern  the pattern to search for, not null
   * @return the map, not null
   */
  private Map<File, Map<Integer, File>> findScriptFiles(File dbCreateSuperDir, final Pattern scriptPattern) {
    ArgumentChecker.isTrue(dbCreateSuperDir.exists(), "Directory " + dbCreateSuperDir.getAbsolutePath() + " does not exist");
    ArgumentChecker.isTrue(dbCreateSuperDir.isDirectory(), "dbCreateSuperDir must be directory not a regular file");
    
    // find the candidate directories, such as foo_db
    final File[] dbCreateScriptDirs = dbCreateSuperDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return matches(pathname.getName(), DATABASE_SCRIPT_FOLDER_PATTERN);
      }
    });
    
    // find the versioned files
    Map<File, Map<Integer, File>> dbFolder2versionedScripts = newHashMap();
    for (File dbCreateScriptDir : dbCreateScriptDirs) {
      final File[] scriptFiles = dbCreateScriptDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          return pathname.isFile()
            && matches(pathname.getName(), scriptPattern);
        }
      });
      Map<Integer, File> versionedScripts = newHashMap();
      for (File scriptFile : scriptFiles) {
        String versionStr = extract(scriptFile.getName(), scriptPattern, 1);
        Integer version = Integer.valueOf(versionStr);
        versionedScripts.put(version, scriptFile);
      }
      dbFolder2versionedScripts.put(dbCreateScriptDir, versionedScripts);
    }
    return dbFolder2versionedScripts;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getDatabaseType();
  }

}
