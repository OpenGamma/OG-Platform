/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.threeten.bp.Clock;

import com.opengamma.elsql.ElSqlBundle;
import com.opengamma.util.db.DbConnector;

/**
 * Provides access to configuration methods on {@link AbstractDbMaster} (and
 * potentially other implementations). This makes it possible to initialize
 * in a standard way. See AbstractDbMasterComponentFactory (og-component)
 * for further details.
 */
public interface ConfigurableDbMaster {

  /**
   * Sets the scheme in use for unique identifier.
   * 
   * @param scheme  the scheme for unique identifier, not null
   */
  void setUniqueIdScheme(final String scheme);

  /**
   * Sets the clock that determines the current time.
   * 
   * @param clock  the clock, not null
   */
  void setClock(final Clock clock);

  /**
   * Sets the external SQL bundle.
   * 
   * @param bundle  the external SQL bundle, not null
   */
  void setElSqlBundle(ElSqlBundle bundle);

  /**
   * Sets the maximum number of retries.
   * The default is ten.
   *
   * @param maxRetries  the maximum number of retries, not negative
   */
  void setMaxRetries(final int maxRetries);

  /**
   * Retrieves the version of the master schema from the database.
   *  
   * @return the schema version, or null if not found
   */
  Integer getSchemaVersion();

  /**
   * Gets the scheme in use for unique identifier.
   * 
   * @return the scheme, not null
   */
  String getUniqueIdScheme();

  /**
   * Gets the clock that determines the current time.
   * 
   * @return the clock, not null
   */
  Clock getClock();

  /**
   * Gets the external SQL bundle.
   * 
   * @return the external SQL bundle, not null
   */
  ElSqlBundle getElSqlBundle();

  /**
   * Gets the database connector.
   * 
   * @return the database connector, not null
   */
  DbConnector getDbConnector();

  /**
   * Gets the maximum number of retries.
   * The default is ten.
   *
   * @return the maximum number of retries, not null
   */
  int getMaxRetries();

}
