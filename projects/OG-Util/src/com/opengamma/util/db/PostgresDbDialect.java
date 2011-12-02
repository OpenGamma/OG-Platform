/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

import com.opengamma.extsql.ExtSqlConfig;

/**
 * Database dialect for Postgres databases.
 * <p>
 * This contains any Postgres specific SQL and is tested for version 8.4 and 9.1.
 */
public class PostgresDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final PostgresDbDialect INSTANCE = new PostgresDbDialect();

  /**
   * Restrictive constructor.
   */
  public PostgresDbDialect() {
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

  @Override
  protected ExtSqlConfig createExtSqlConfig() {
    return ExtSqlConfig.POSTGRES;
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
