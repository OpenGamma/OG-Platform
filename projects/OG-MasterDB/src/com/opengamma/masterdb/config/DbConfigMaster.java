/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.config.impl.DefaultConfigMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * Full details of the API are in {@link ConfigTypeMaster}.
 * This class uses JDBC to store the data via a set of workers.
 * The workers may be replaced by configuration to allow different SQL on different databases.
 */
public class DbConfigMaster extends DefaultConfigMaster {

  /**
   * The scheme used for UniqueIdentifier objects.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";

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
  public DbConfigMaster(final DbSource dbSource) {
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
  protected <T> ConfigTypeMaster<T> createTypedMaster(Class<T> clazz) {
    DbConfigTypeMaster<T> master = new DbConfigTypeMaster<T>(clazz, _dbSource);
    if (getIdentifierScheme() != null) {
      master.setIdentifierScheme(getIdentifierScheme());
    }
    return master;
  }

}
