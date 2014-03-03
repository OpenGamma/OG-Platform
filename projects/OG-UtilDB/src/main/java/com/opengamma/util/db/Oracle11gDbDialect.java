/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.elsql.ElSqlConfig;

/**
 * Database dialect for Oracle databases.
 * <p>
 * This contains any Oracle specific SQL and is tested for version 11g express.
 */
public class Oracle11gDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final Oracle11gDbDialect INSTANCE = new Oracle11gDbDialect();

  /**
   * Restrictive constructor.
   */
  public Oracle11gDbDialect() {
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    try {
      return (Class<? extends Driver>) Class.forName("oracle.jdbc.driver.OracleDriver");
    } catch (ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not load the Oracle JDBC driver: " + ex.getMessage());
    }
    // Use the Oracle driver...
    // return oracle.jdbc.driver.OracleDriver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new Oracle10gDialect();
  }

  @Override
  protected ElSqlConfig createElSqlConfig() {
    return ElSqlConfig.ORACLE;
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return "SELECT " + sequenceName + ".nextval FROM DUAL";
  }

  @Override
  public String sqlSelectNow() {
    return "SELECT CURRENT_TIMESTAMP FROM DUAL";
  }

  //-------------------------------------------------------------------------
  @Override
  public LobHandler getLobHandler() {
    DefaultLobHandler handler = new DefaultLobHandler();
    handler.setWrapAsLob(false);
    return handler;
  }

  //-------------------------------------------------------------------------
  /**
   * Closes the Oracle database, initiating a shutdown.
   */
  @Override
  public void close() {
    DatabaseManager.closeDatabases(Database.CLOSEMODE_NORMAL);
  }

}
