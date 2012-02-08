/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb;

import org.testng.annotations.Factory;

import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.AbstractDbUpgradeTest;

/**
 * Tests the database upgrade scripts.
 */
public class MasterDbDatabaseUpgradeTest extends AbstractDbUpgradeTest {

  @Factory(dataProvider = "databasesMoreVersions", dataProviderClass = DbTest.class)
  public MasterDbDatabaseUpgradeTest(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

}
