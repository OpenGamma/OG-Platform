/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.util.ArgumentChecker;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;

// REVIEW kirk 2010-08-06 -- If this is found to be useful outside of the
// caching stuff, it should be moved to OG-Util.

/**
 * A component which wraps a single BerkeleyDB Database.
 */
public abstract class AbstractBerkeleyDBComponent implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBerkeleyDBComponent.class);
  // Injected inputs:
  private final Environment _dbEnvironment;
  private final String _databaseName;

  // Runtime state:
  private final AtomicBoolean _started = new AtomicBoolean(false);
  private Database _database;
  
  protected AbstractBerkeleyDBComponent(Environment dbEnvironment, String databaseName) {
    ArgumentChecker.notNull(dbEnvironment, "dbEnvironment");
    ArgumentChecker.notNull(databaseName, "databaseName");
    _dbEnvironment = dbEnvironment;
    _databaseName = databaseName;
  }

  /**
   * Gets the dbEnvironment field.
   * @return the dbEnvironment
   */
  public Environment getDbEnvironment() {
    return _dbEnvironment;
  }

  /**
   * Gets the databaseName field.
   * @return the databaseName
   */
  public String getDatabaseName() {
    return _databaseName;
  }

  /**
   * Gets the database field.
   * @return the database
   */
  protected Database getDatabase() {
    return _database;
  }

  /**
   * Sets the database field.
   * @param database  the database
   */
  private void setDatabase(Database database) {
    _database = database;
  }

  @Override
  public boolean isRunning() {
    return _started.get();
  }

  @Override
  public void start() {
    s_logger.info("Starting, and opening Database.");
    DatabaseConfig dbConfig = getDatabaseConfig();
    Database database = getDbEnvironment().openDatabase(null, getDatabaseName(), dbConfig);
    setDatabase(database);
    postStartInitialization();
    _started.set(true);
  }
  
  protected abstract DatabaseConfig getDatabaseConfig();
  
  protected void postStartInitialization() {
    
  }

  @Override
  public void stop() {
    s_logger.info("Shutting down Database.");
    if (getDatabase() != null) {
      getDatabase().close();
    }
    _started.set(false);
  }

}
