/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import com.opengamma.master.config.ConfigTypeMaster;
import com.opengamma.master.config.impl.DefaultConfigMaster;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbSource;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the config master using an SQL database.
 * See {@link DbConfigTypeMaster} for the main class.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 */
public class DbConfigMaster extends DefaultConfigMaster implements NotifyingMaster {

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
   * The change manager.
   */
  private MasterChangeManager _changeManager = new BasicMasterChangeManager();

  /**
   * Creates an instance.
   * 
   * @param dbSource  the database source, not null
   */
  public DbConfigMaster(final DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    _dbSource = dbSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the scheme in use for UniqueIdentifier.
   * 
   * @return the scheme, not null
   */
  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  /**
   * Sets the scheme in use for UniqueIdentifier.
   * 
   * @param scheme  the scheme, not null
   */
  public void setIdentifierScheme(final String scheme) {
    ArgumentChecker.notNull(scheme, "scheme");
    _identifierScheme = scheme;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the change manager.
   * 
   * @return the change manager, not null
   */
  public MasterChangeManager getChangeManager() {
    return _changeManager;
  }

  /**
   * Sets the change manager.
   * 
   * @param changeManager  the change manager, not null
   */
  public void setChangeManager(final MasterChangeManager changeManager) {
    ArgumentChecker.notNull(changeManager, "changeManager");
    _changeManager = changeManager;
  }

  //-------------------------------------------------------------------------
  @Override
  protected <T> ConfigTypeMaster<T> createTypedMaster(Class<T> clazz) {
    DbConfigTypeMaster<T> master = new DbConfigTypeMaster<T>(clazz, _dbSource, getChangeManager());
    if (getIdentifierScheme() != null) {
      master.setIdentifierScheme(getIdentifierScheme());
    }
    return master;
  }

  //-------------------------------------------------------------------------
  @Override
  public MasterChangeManager changeManager() {
    return getChangeManager();  // events from all config type masters
  }

}
