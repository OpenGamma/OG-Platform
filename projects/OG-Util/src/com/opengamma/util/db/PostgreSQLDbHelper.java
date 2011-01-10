/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

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
  public Class<? extends Driver> getJDBCDriverClass() {
    return org.postgresql.Driver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new PostgreSQLDialect();
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "SELECT nextval('" + sequenceName + "')";
  }

  @Override
  public String sqlNextSequenceValueInline(final String sequenceName) {
    return "nextval('" + sequenceName + "')";
  }

}
