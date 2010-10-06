/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.test.DBUpgradeTest;

/**
 * Tests the database upgrade scripts.
 */
public class DatabaseUpgradeTest extends DBUpgradeTest {
  private static final Logger s_logger = LoggerFactory.getLogger(DatabaseUpgradeTest.class);

  public DatabaseUpgradeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.debug("running test for databaseType={} databaseVersion={}", databaseType, databaseVersion);
  }

}
