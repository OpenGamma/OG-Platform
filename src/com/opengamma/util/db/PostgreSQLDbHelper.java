/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

/**
 * Helper for working with the PostgreSQL database.
 * <p>
 * This contains any PostgreSQL specific SQL and is tested for version 8.4.
 */
public class PostgreSQLDbHelper extends DbHelper {

  /**
   * Helper can be treated as a singleton.
   */
  public static final PostgreSQLDbHelper INSTANCE = new PostgreSQLDbHelper();

  /**
   * Restrictive constructor.
   */
  public PostgreSQLDbHelper() {
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "SELECT nextval(" + sequenceName + ")";
  }

  @Override
  public String sqlNextSequenceValueInline(final String sequenceName) {
    return "nextval(" + sequenceName + ")";
  }

}
