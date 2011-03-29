/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.testng.annotations.Factory;

import com.opengamma.util.test.DBTest;
import com.opengamma.util.test.DBUpgradeTest;

/**
 * Tests the database upgrade scripts.
 */
public class MasterDBDatabaseUpgradeTest extends DBUpgradeTest {

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DBTest.class)
  public MasterDBDatabaseUpgradeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

}
