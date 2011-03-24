/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * An implementation of {@link ViewComputationCacheSource} which will use an injected
 * {@link IdentifierMap} and construct {@link BerkeleyDBValueSpecificationBinaryDataStore}
 * instances on demand to satisfy cache requests.
 */
public class BerkeleyDBViewComputationCacheSource extends DefaultViewComputationCacheSource {

  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBViewComputationCacheSource.class);

  private static Environment constructDatabaseEnvironmentImpl(final File dbDir, final boolean transactional) {
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }
    EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(transactional);
    return new Environment(dbDir, envConfig);
  }

  private static void deleteFile(final File file) {
    if (file.isDirectory()) {
      for (File subfile : file.listFiles()) {
        deleteFile(subfile);
      }
    }
    file.delete();
  }

  public static Environment constructDatabaseEnvironment(File dbDir, boolean transactional) {
    try {
      return constructDatabaseEnvironmentImpl(dbDir, transactional);
    } catch (RuntimeException e) {
      s_logger.warn("Error creating DB environment, deleting {} and trying again", dbDir);
      deleteFile(dbDir);
      return constructDatabaseEnvironmentImpl(dbDir, transactional);
    }
  }

  public BerkeleyDBViewComputationCacheSource(IdentifierMap identifierMap, Environment dbEnvironment, FudgeContext fudgeContext) {
    super(identifierMap, fudgeContext, new DefaultFudgeMessageStoreFactory(new BerkeleyDBBinaryDataStoreFactory(
        dbEnvironment), fudgeContext));
  }

}
