/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.util.ReflectionUtils;

/**
 * Database dialect for Vertica databases.
 * <p>
 * This contains any Vertica specific SQL and is based on Postgres.
 */
public class VerticaDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final VerticaDbDialect INSTANCE = new VerticaDbDialect();

  /**
   * Restrictive constructor.
   */
  public VerticaDbDialect() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    return ReflectionUtils.loadClass("com.vertica.Driver");
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new PostgreSQLDialect();  // nearest match
  }

  @Override
  protected ElSqlConfig createElSqlConfig() {
    return ElSqlConfig.VERTICA;
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
