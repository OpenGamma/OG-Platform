/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.DerbyDialect;
import org.hibernate.dialect.Dialect;

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

  //-------------------------------------------------------------------------
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    return org.apache.derby.jdbc.EmbeddedDriver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new DerbyDialect();
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "SELECT NEXT VALUE FOR " + sequenceName + " FROM sysibm.sysdummy1";
  }

}
