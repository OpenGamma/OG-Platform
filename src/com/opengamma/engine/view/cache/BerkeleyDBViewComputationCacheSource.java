/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * An implementation of {@link ViewComputationCacheSource} which will use an injected
 * {@link IdentifierMap} and construct {@link BerkeleyDBValueSpecificationBinaryDataStore}
 * instances on demand to satisfy cache requests.
 */
public class BerkeleyDBViewComputationCacheSource extends DefaultViewComputationCacheSource {

  public static Environment constructDatabaseEnvironment(File dbDir, boolean transactional) {
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(transactional);
    Environment dbEnvironment = new Environment(dbDir, envConfig);
    return dbEnvironment;
  }

  public BerkeleyDBViewComputationCacheSource(IdentifierMap identifierMap, Environment dbEnvironment, FudgeContext fudgeContext) {
    super(identifierMap, fudgeContext, new BerkeleyDBBinaryDataStoreFactory(dbEnvironment));
  }

}
