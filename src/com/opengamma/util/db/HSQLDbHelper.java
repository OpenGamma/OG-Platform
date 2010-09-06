/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

/**
 * Helper for working with the Hypersonic SQL database.
 */
public class HSQLDbHelper extends DbHelper {

  /**
   * Helper can be treated as a singleton.
   */
  public static final HSQLDbHelper INSTANCE = new HSQLDbHelper();

  /**
   * Restrictive constructor.
   */
  public HSQLDbHelper() {
  }
  
  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "CALL NEXT VALUE FOR " + sequenceName;
  }


}
