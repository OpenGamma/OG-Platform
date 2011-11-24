/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.Dialect;

import com.opengamma.extsql.ExtSqlConfig;

/**
 * Database dialect for Derby databases.
 * <p>
 * This contains any Derby specific SQL and is tested for version 10.6.
 * Sequences were added in 10.6. Offset/Fetch were added in 10.5.
 */
public class DerbyDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final DerbyDbDialect INSTANCE = new DerbyDbDialect();

  /**
   * Restrictive constructor.
   */
  public DerbyDbDialect() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    return org.apache.derby.jdbc.EmbeddedDriver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new DerbyDialect();
  }

  @Override
  protected ExtSqlConfig createExtSqlConfig() {
    return ExtSqlConfig.DEFAULT;  // not tested for Derby
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "SELECT NEXT VALUE FOR " + sequenceName + " FROM sysibm.sysdummy1";
  }

}
