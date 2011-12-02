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

import com.opengamma.extsql.ExtSqlConfig;

/**
 * Database dialect for HSQL databases.
 * <p>
 * This contains any HSQL specific SQL and is tested for version 2.2.5.
 */
public class HSQLDbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final HSQLDbDialect INSTANCE = new HSQLDbDialect();

  /**
   * Restrictive constructor.
   */
  public HSQLDbDialect() {
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

  @Override
  protected ExtSqlConfig createExtSqlConfig() {
    return ExtSqlConfig.HSQL;
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

  /**
   * Returns the prefix with the correct wildcard search type.
   * Returns 'prefix LIKE paramName ' if there are wildcards,
   * 'prefix = paramName ' if no wildcards and '' if null.
   * The prefix is normally 'AND columnName ' or 'OR columnName '.
   * 
   * @param prefix  the prefix such as 'AND columnName ', not null
   * @param paramName  the parameter name normally prefixed by colon, not null
   * @param value  the string value, may be null
   * @return the SQL fragment, not null
   */
  public String sqlWildcardQuery(final String prefix, final String paramName, final String value) {
    if (value == null) {
      return "";
    } else if (isWildcard(value)) {
      return prefix + "LIKE " + paramName + " ESCAPE '\\' ";
    } else {
      return prefix + "= " + paramName + ' ';
    }
  }

}
