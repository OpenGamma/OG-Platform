/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * An implementation of {@link ViewComputationCacheSource} which will use an injected
 * {@link ValueSpecificationIdentifierSource} and construct {@link BerkeleyDBValueSpecificationBinaryDataStore}
 * instances on demand to satisfy cache requests.
 */
public class BerkeleyDBViewComputationCacheSource extends AbstractViewComputationCacheSource {
  private final Environment _dbEnvironment;
  
  public BerkeleyDBViewComputationCacheSource(
      ValueSpecificationIdentifierSource identifierSource, File dbDir, FudgeContext fudgeContext) {
    super(identifierSource, fudgeContext);
    ArgumentChecker.notNull(dbDir, "Database Directory");
    _dbEnvironment = constructDatabaseEnvironment(dbDir);
  }
  
  protected Environment constructDatabaseEnvironment(File dbDir) {
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(false);
    Environment dbEnvironment = new Environment(dbDir, envConfig);
    return dbEnvironment;
  }

  /**
   * Gets the dbEnvironment field.
   * @return the dbEnvironment
   */
  public Environment getDbEnvironment() {
    return _dbEnvironment;
  }

  protected ValueSpecificationIdentifierBinaryDataStore constructDataStore(ViewComputationCacheKey key) {
    String dbName = key.getViewName() + "-" + key.getCalculationConfigurationName() + "-" + key.getSnapshotTimestamp();
    BerkeleyDBValueSpecificationIdentifierBinaryDataStore dataStore = new BerkeleyDBValueSpecificationIdentifierBinaryDataStore(getDbEnvironment(), dbName);
    dataStore.start();
    return dataStore;
  }

}
