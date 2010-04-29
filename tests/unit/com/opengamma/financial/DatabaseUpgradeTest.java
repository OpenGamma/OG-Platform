/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.util.test.DBUpgradeTest;

/**
 * Tests the database upgrade scripts.
 * 
 * @author Andrew Griffin
 */
public class DatabaseUpgradeTest extends DBUpgradeTest {
  
  public DatabaseUpgradeTest (final String databaseType, final String databaseVersion) {
    super (databaseType, databaseVersion);
  }
  
}