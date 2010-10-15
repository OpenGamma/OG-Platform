/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import com.opengamma.config.ConfigMaster;
import com.opengamma.config.db.DbConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A config source built using the database.
 * <p>
 * This class creates instances of {@code DbConfigMaster} on demand and caches them permanently.
 */
public class DbMasterConfigSource extends MasterConfigSource {

  /**
   * The source of database connections.
   */
  private final DbSource _dbSource;

  /**
   * Creates an instance.
   * @param dbSource  the database source, not null
   */
  public DbMasterConfigSource(final DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    _dbSource = dbSource;
  }

  //-------------------------------------------------------------------------
  @Override
  protected <T> ConfigMaster<T> createMaster(final Class<T> clazz) {
    return new DbConfigMaster<T>(clazz, _dbSource);
  }

}
