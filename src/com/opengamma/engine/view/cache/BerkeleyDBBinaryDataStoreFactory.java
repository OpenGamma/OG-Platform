/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.File;

import com.opengamma.util.ArgumentChecker;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * Creates {@link BerkeleyDBBinaryDataStore} instances.
 */
public class BerkeleyDBBinaryDataStoreFactory implements BinaryDataStoreFactory {

  private final Environment _dbEnvironment;

  public BerkeleyDBBinaryDataStoreFactory(final File dbDir) {
    ArgumentChecker.notNull(dbDir, "Database Directory");
    _dbEnvironment = constructDatabaseEnvironment(dbDir);
  }

  @Override
  public BinaryDataStore createDataStore(ViewComputationCacheKey cacheKey) {
    String dbName = cacheKey.getViewName() + "-" + cacheKey.getCalculationConfigurationName() + "-" + cacheKey.getSnapshotTimestamp();
    BerkeleyDBBinaryDataStore dataStore = new BerkeleyDBBinaryDataStore(_dbEnvironment, dbName);
    dataStore.start();
    return dataStore;
  }

  private static Environment constructDatabaseEnvironment(File dbDir) {
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(false);
    Environment dbEnvironment = new Environment(dbDir, envConfig);
    return dbEnvironment;
  }

}
