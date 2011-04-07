/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

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

  @Override
  public String sqlSelectNow() {
    return "SELECT * FROM (VALUES(current_timestamp)) AS V(NOW_TIMESTAMP)";
  }

  //-------------------------------------------------------------------------
  @Override
  public LobHandler getLobHandler() {
    DefaultLobHandler handler = new DefaultLobHandler();
    handler.setWrapAsLob(true);
    return handler;
  }

}
