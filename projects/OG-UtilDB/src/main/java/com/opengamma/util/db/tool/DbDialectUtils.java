/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.HSQLDbDialect;
import com.opengamma.util.db.Oracle11gDbDialect;
import com.opengamma.util.db.PostgresDbDialect;
import com.opengamma.util.db.SqlServer2008DbDialect;
import com.opengamma.util.time.DateUtils;

/**
 * Utilities to support database testing.
 */
public final class DbDialectUtils {

  /** Known dialects. */
  private static final Map<String, DbDialect> s_dbDialects = new ConcurrentHashMap<>();
  /** Available dialects. */
  private static final Map<String, Boolean> s_availableDialects = new ConcurrentHashMap<String, Boolean>();

  static {
    // initialize the clock
    DateUtils.initTimeZone();
    
    // setup the known databases
    addDbDialect("hsqldb", new HSQLDbDialect());
    addDbDialect("postgres", new PostgresDbDialect());
    addDbDialect("sqlserver2008", new SqlServer2008DbDialect());
    addDbDialect("oracle11g", new Oracle11gDbDialect());
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   */
  private DbDialectUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the supported database types.
   * 
   * @return the supported database types, not null
   */
  public static Collection<String> getSupportedDatabaseTypes() {
    return new ArrayList<>(s_dbDialects.keySet());
  }

  /**
   * Gets the supported database types as {@code DbDialect} objects.
   *
   * @return the supported database dialects keyed by type, not null
   */
  public static Map<String, DbDialect> getSupportedDbDialects() {
    return new HashMap<>(s_dbDialects);
  }

  /**
   * Gets a supported database dialect by type name.
   * 
   * @param databaseType  the database type, not null
   * @return the dialect, not null
   */
  public static DbDialect getSupportedDbDialect(String databaseType) {
    DbDialect dbDialect = getSupportedDbDialects().get(databaseType);
    if (dbDialect == null) {
      throw new OpenGammaRuntimeException("Config error - no DbDialect setup for " + databaseType);
    }
    return dbDialect;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the available database types.
   * <p>
   * Not all supported database types may be available at runtime.
   * 
   * @return the available database types, not null
   */
  public static Collection<String> getAvailableDatabaseTypes() {
    Collection<String> databaseTypes = Sets.newHashSet(s_dbDialects.keySet());
    for (Iterator<String> it = databaseTypes.iterator(); it.hasNext(); ) {
      String dbType = it.next();
      Boolean available = s_availableDialects.get(dbType);
      if (available == null) {
        DbDialect dbDialect = s_dbDialects.get(dbType);
        try {
          Objects.requireNonNull(dbDialect.getJDBCDriverClass());
          available = true;
        } catch (RuntimeException | Error ex) {
          available = false;
          System.err.println("Database driver not available: " + dbType);
        }
        s_availableDialects.put(dbType, available);
      }
      if (available == false) {
        it.remove();
      }
    }
    return databaseTypes;
  }

  /**
   * Gets the available database types as {@code DbDialect} objects.
   * <p>
   * Not all supported database types may be available at runtime.
   * 
   * @return the available database dialects keyed by type, not null
   */
  public static Map<String, DbDialect> getAvailableDbDialects() {
    Collection<String> availableTypes = getAvailableDatabaseTypes();
    Map<String, DbDialect> available = Maps.newHashMap();
    for (String availableType : availableTypes) {
      available.put(availableType, s_dbDialects.get(availableType));
    }
    return available;
  }

  /**
   * Gets an available database dialect by type name.
   * 
   * @param databaseType  the database type, not null
   * @return the dialect, not null
   */
  public static DbDialect getAvailableDbDialect(String databaseType) {
    DbDialect dbDialect = getAvailableDbDialects().get(databaseType);
    if (dbDialect == null) {
      throw new OpenGammaRuntimeException("Config error - no available DbDialect for " + databaseType);
    }
    return dbDialect;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a dialect to the map of known.
   *
   * @param dbType  the database type, not null
   * @param dialect  the dialect, not null
   */
  public static void addDbDialect(String dbType, DbDialect dialect) {
    s_dbDialects.put(dbType, dialect);
  }

}
