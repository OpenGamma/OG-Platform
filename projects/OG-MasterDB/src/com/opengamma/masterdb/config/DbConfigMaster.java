/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.db.DbConnector;

/**
 * A config master implementation using a database for persistence.
 * <p>
 * This is a full implementation of the config master using an SQL database.
 * Full details of the API are in {@link ConfigMaster}.
 * <p>
 * The SQL is stored externally in {@code DbConfigMaster.elsql}.
 * Alternate databases or specific SQL requirements can be handled using database
 * specific overrides, such as {@code DbConfigMaster-MySpecialDB.elsql}.
 * <p>
 * This class is mutable but must be treated as immutable after configuration.
 *
 */
public class DbConfigMaster extends DbConfigWorker implements ConfigMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbCfg";

  /**
   * Creates an instance.
   *
   * @param dbConnector  the database connector, not null
   */
  public DbConfigMaster(DbConnector dbConnector) {
    super(dbConnector, IDENTIFIER_SCHEME_DEFAULT);
  }

}
