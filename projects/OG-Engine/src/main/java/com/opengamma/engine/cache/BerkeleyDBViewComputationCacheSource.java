/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import java.io.File;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * An implementation of {@link ViewComputationCacheSource} which will use an injected
 * {@link IdentifierMap} and construct {@link DefaultViewComputationCache}
 * instances on demand to satisfy cache requests.
 */
public class BerkeleyDBViewComputationCacheSource extends DefaultViewComputationCacheSource {

  private static final Logger s_logger = LoggerFactory.getLogger(BerkeleyDBViewComputationCacheSource.class);

  private static Environment constructDatabaseEnvironmentImpl(final File dbDir, final boolean transactional) {
    if (!dbDir.exists()) {
      dbDir.mkdirs();
    }
    final EnvironmentConfig envConfig = new EnvironmentConfig();
    envConfig.setAllowCreate(true);
    envConfig.setTransactional(transactional);
    return new Environment(dbDir, envConfig);
  }

  private static void deleteFile(final File file) {
    if (file.isDirectory()) {
      for (final File subfile : file.listFiles()) {
        deleteFile(subfile);
      }
    }
    file.delete();
  }

  public static Environment constructDatabaseEnvironment(final File dbDir, final boolean transactional) {
    try {
      return constructDatabaseEnvironmentImpl(dbDir, transactional);
    } catch (final RuntimeException e) {
      s_logger.warn("Error creating DB environment, deleting {} and trying again", dbDir);
      deleteFile(dbDir);
      return constructDatabaseEnvironmentImpl(dbDir, transactional);
    }
  }

  public BerkeleyDBViewComputationCacheSource(final IdentifierMap identifierMap, final Environment dbEnvironment, final FudgeContext fudgeContext) {
    super(identifierMap, fudgeContext, new DefaultFudgeMessageStoreFactory(new BerkeleyDBBinaryDataStoreFactory(
        dbEnvironment), fudgeContext));
  }

}
