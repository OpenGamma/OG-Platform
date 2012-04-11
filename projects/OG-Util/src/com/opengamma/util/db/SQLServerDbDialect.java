/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.elsql.ElSqlConfig;

/**
 * Database dialect for SQL Server databases.
 * <p>
 * This contains any SQL Server specific SQL.
 */
public class SQLServerDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final SQLServerDbDialect INSTANCE = new SQLServerDbDialect();

  /**
   * Restrictive constructor.
   */
  public SQLServerDbDialect() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    return com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new SQLServerDialect();
  }

  @Override
  protected ElSqlConfig createElSqlConfig() {
    return ElSqlConfig.SQLSERVER;
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "EXECUTE nextval_" + sequenceName;
  }

  @Override
  public String sqlNextSequenceValueInline(final String sequenceName) {
    throw new OpenGammaRuntimeException("sqlNextSequenceValueInline not available for the SQL Server 2008 dialect");
  }

}
