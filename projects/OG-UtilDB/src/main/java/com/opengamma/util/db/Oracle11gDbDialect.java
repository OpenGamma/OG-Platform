/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import javax.sql.DataSource;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hsqldb.Database;
import org.hsqldb.DatabaseManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.google.common.base.CharMatcher;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.elsql.ElSqlConfig;

/**
 * Database dialect for Oracle databases.
 * <p>
 * This contains any Oracle specific SQL and is tested for version 11g express.
 * <p>
 * An empty string is treated as null in Oracle.
 * The {@link #toDatabaseString(String)} and {@link #fromDatabaseString(String)}
 * methods allow the distinction to be preserved.
 * They replace an empty string by a single character string.
 * Any string consisting of all spaces is replaced by a string with one additional space.
 */
public class Oracle11gDbDialect extends DbDialect {
  /**
   * Helper can be treated as a singleton.
   */
  public static final Oracle11gDbDialect INSTANCE = new Oracle11gDbDialect();
  /**
   * Matcher for a string that is all spaces.
   */
  private static final CharMatcher ALL_SPACES = CharMatcher.is(' ');

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
  public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(DataSource dataSource) {
    return new OracleNamedParameterJdbcTemplate(dataSource);
  }

  @Override
  public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
    return new OracleJdbcTemplate(dataSource);
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
  public String toDatabaseString(String str) {
    if (str == null) {
      return null;
    } else if (str.isEmpty()) {
      return " ";
    } else if (str.charAt(0) != ' ') {
      return str;
    } else if (ALL_SPACES.matchesAllOf(str)) {
      return str + ' ';
    } else {
      return str;
    }
  }

  @Override
  public String fromDatabaseString(String str) {
    if (str == null) {
      return null;
    } else if (str.length() > 0 && ALL_SPACES.matchesAllOf(str)) {
      return str.substring(1);
    } else {
      return str;
    }
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
