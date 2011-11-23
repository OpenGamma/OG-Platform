/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.apache.commons.lang.StringUtils;
import org.hibernate.dialect.Dialect;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import com.opengamma.extsql.ExtSqlConfig;
import com.opengamma.util.paging.PagingRequest;

/**
 * Operations that support the working with different database dialects.
 * <p>
 * There will typically be a different implementation of this class for
 * each different database and potentially for different versions.
 */
public abstract class DbDialect {

  /**
   * The cached hibernate dialect.
   */
  private volatile Dialect _hibernateDialect;
  /**
   * The config for the ExtSql system.
   */
  private volatile ExtSqlConfig _extSqlConfig;

  /**
   * Restrictive constructor.
   */
  protected DbDialect() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the database.
   * 
   * @return the name of the database
   */
  public String getName() {
    String name = getClass().getSimpleName();
    int endPos = name.lastIndexOf("DbDialect");
    return (endPos < 0 ? name : name.substring(0, endPos));
  }

  /**
   * Gets the JDBC driver class.
   * 
   * @return the driver, not null
   */
  public abstract Class<? extends Driver> getJDBCDriverClass();

  //-------------------------------------------------------------------------
  /**
   * Gets the Hibernate dialect object for the database.
   * 
   * @return the dialect, not null
   */
  public final Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = createHibernateDialect();
    }
    return _hibernateDialect;
  }

  /**
   * Creates the Hibernate dialect object for the database.
   * This will be cached by the base class.
   * 
   * @return the dialect, not null
   */
  protected abstract Dialect createHibernateDialect();

  //-------------------------------------------------------------------------
  /**
   * Gets the ExtSql config.
   * 
   * @return the config, not null
   */
  public final ExtSqlConfig getExtSqlConfig() {
    if (_extSqlConfig == null) {
      _extSqlConfig = createExtSqlConfig();
    }
    return _extSqlConfig;
  }

  /**
   * Creates the ExtSql config object for the database.
   * This will be cached by the base class.
   * 
   * @return the config, not null
   */
  protected abstract ExtSqlConfig createExtSqlConfig();

  //-------------------------------------------------------------------------
  /**
   * Checks if the string contains wildcard characters.
   * 
   * @param str  the string to check, null returns false
   * @return true if the string contains wildcards
   */
  public boolean isWildcard(final String str) {
    return str != null && (str.contains("*") || str.contains("?"));
  }

  /**
   * Returns 'LIKE' if there are wildcards, or '=' otherwise.
   * 
   * @param str  the string to check, null returns '='
   * @return the wildcard operator, not surrounded with spaces
   */
  public String sqlWildcardOperator(final String str) {
    return isWildcard(str) ? "LIKE" : "=";
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
      return prefix + "LIKE " + paramName + ' ';
    } else {
      return prefix + "= " + paramName + ' ';
    }
  }

  /**
   * Adjusts wildcards from the public values of * and ? to the database
   * values of % and _.
   * 
   * @param value  the string value, may be null
   * @return the SQL fragment, not null
   */
  public String sqlWildcardAdjustValue(String value) {
    if (value == null || isWildcard(value) == false) {
      return value;
    }
    value = StringUtils.replace(value, "%", "\\%");
    value = StringUtils.replace(value, "_", "\\_");
    value = StringUtils.replace(value, "*", "%");
    value = StringUtils.replace(value, "?", "_");
    return value;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds SQL to apply paging.
   * 
   * @param sqlSelectFromWhere  the SQL select from where ending in space, not null
   * @param sqlOrderBy  the SQL order by ending in space, not null
   * @param paging  the paging request, may be null
   * @return the combined SQL, space terminated, not null
   */
  public String sqlApplyPaging(final String sqlSelectFromWhere, final String sqlOrderBy, final PagingRequest paging) {
    if (paging == null || paging.equals(PagingRequest.ALL) || paging.equals(PagingRequest.NONE)) {
      return sqlSelectFromWhere + sqlOrderBy;
    }
    // use SQL standard
    // works on Postgres 8.4 onwards, Derby 10.5 onwards
    // OFFSET ... FETCH ... needs to be fully wordy to satisfy Derby
    // MySQL uses LIMIT ... OFFSET ...
    // Others use window functions (more complex)
    if (paging.getFirstItem() == 0) {
      return sqlSelectFromWhere + sqlOrderBy +
          "FETCH FIRST " + paging.getPagingSize() + " ROWS ONLY ";
    }
    return sqlSelectFromWhere + sqlOrderBy +
        "OFFSET " + paging.getFirstItem() + " ROWS " +
        "FETCH NEXT " + paging.getPagingSize() + " ROWS ONLY ";
  }

  //-------------------------------------------------------------------------
  /**
   * Builds SQL to query a sequence (typically created with CREATE SEQUENCE).
   * 
   * @param sequenceName  the sequence name, not null
   * @return the SQL, not space terminated, not null
   */
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    // use SQL standard
    // works on Derby 10.6 onwards
    // Derby uses SELECT NEXT VALUE FOR seq_name FROM sysibm.sysdummy1
    // Postgres uses SELECT nextval(seq_name)
    // Oracle uses SELECT seq_name.NEXTVAL FROM dual
    return "SELECT NEXT VALUE FOR " + sequenceName;
  }

  /**
   * Builds SQL to query a sequence (typically created with CREATE SEQUENCE).
   * 
   * @param sequenceName  the sequence name, not null
   * @return the SQL, space terminated, not null
   */
  public String sqlNextSequenceValueInline(final String sequenceName) {
    // use SQL standard
    // works on Derby 10.6 onwards
    // NEXT VALUE FOR seq_name
    // Postgres uses nextval(seq_name)
    // Oracle uses seq_name.NEXTVAL
    return "NEXT VALUE FOR " + sequenceName + " ";
  }

  //-------------------------------------------------------------------------
  /**
   * Builds SQL to query the current timestamp.
   * This is typically the start of the transaction.
   * The column name to read is "NOW_TIMESTAMP".
   * 
   * @return the entire SQL select clause, not space terminated, not null
   */
  public String sqlSelectNow() {
    // use SQL standard
    // works on Postgres and MySQL
    // Oracle uses SELECT systimestamp FROM dual
    return "SELECT CURRENT_TIMESTAMP AS NOW_TIMESTAMP";
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the SQL of the coalesce function.
   * This is typically 'COALESCE(a, b, c)' where a, b and c are the input arguments.
   * The input is inserted directly into the function.
   * 
   * @param fragment1  the input SQL fragment, not null
   * @param fragment2  the input SQL fragment, not null
   * @return the SQL, not space terminated, not null
   */
  public String sqlNullDefault(String fragment1, String fragment2) {
    // use SQL standard
    // works on Postgres, Oracle, HSQL and MySQL
    return "COALESCE(" + fragment1 + ", " + fragment2 + ")";
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the LOB handler used for BLOBs and CLOBs.
   * Subclasses will return different handlers for different dialects.
   * 
   * @return the LOB handler, not null
   */
  public LobHandler getLobHandler() {
    return new DefaultLobHandler();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
