/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

/**
 * Helper for working with the Derby database.
 * <p>
 * This contains any Derby specific SQL and is tested for version 10.6.
 * Sequences were added in 10.6. Offset/Fetch were added in 10.5.
 */
public class DerbyDbHelper extends DbHelper {

  /**
   * Helper can be treated as a singleton.
   */
  public static final DerbyDbHelper INSTANCE = new DerbyDbHelper();

  /**
   * Restrictive constructor.
   */
  public DerbyDbHelper() {
  }

}
