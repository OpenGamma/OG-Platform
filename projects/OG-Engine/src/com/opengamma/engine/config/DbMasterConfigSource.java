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
   * The scheme in use for UniqueIdentifier, null for default.
   */
  private String _identifierScheme;

  /**
   * Creates an instance.
   * @param dbSource  the database source, not null
   */
  public DbMasterConfigSource(final DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    _dbSource = dbSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _identifierScheme = scheme;
  }

  //-------------------------------------------------------------------------
  @Override
  protected <T> ConfigMaster<T> createMaster(final Class<T> clazz) {
    DbConfigMaster<T> master = new DbConfigMaster<T>(clazz, _dbSource);
    if (getIdentifierScheme() != null) {
      master.setIdentifierScheme(getIdentifierScheme());
    }
    return master;
  }

}
