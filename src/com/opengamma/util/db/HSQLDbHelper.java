/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;

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
  public Class<? extends Driver> getJDBCDriverClass() {
    return org.hsqldb.jdbcDriver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new HSQLDialect();
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "CALL NEXT VALUE FOR " + sequenceName;
  }


}
