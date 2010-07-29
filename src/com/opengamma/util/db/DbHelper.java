/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import org.apache.commons.lang.StringUtils;

/**
 * Helper for working with databases with subclasses for different databases.
 */
public abstract class DbHelper {

  /**
   * Restrictive constructor.
   */
  protected DbHelper() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the database
   * @return the name of the database
   */
  public String getName() {
    String name = getClass().getSimpleName();
    int endPos = name.lastIndexOf("DbHelper");
    return (endPos < 0 ? name : name.substring(0, endPos));
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the string contains wildcard characters.
   * @param str  the string to check, null returns false
   * @return true if the string contains wildcards
   */
  public boolean isWildcard(final String str) {
    return str != null && (str.contains("*") || str.contains("?"));
  }

  /**
   * Returns 'LIKE' if there are wildcards, or '=' otherwise.
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
   * @param sqlSelectFromWhere  the SQL select from where ending in space, not null
   * @param sqlOrderBy  the SQL order by ending in space, not null
   * @param paging  the paging request, may be null
   * @return the combined SQL, space terminated, not null
   */
  public String sqlApplyPaging(final String sqlSelectFromWhere, final String sqlOrderBy, final PagingRequest paging) {
    if (paging == null || paging.equals(PagingRequest.ALL)) {
      return sqlSelectFromWhere + sqlOrderBy;
    }
    // use SQL standard
    // works on Postgres 8.4 onwards, Derby 10.5 onwards
    // OFFSET ... FETCH ... needs to be fully wordy to satisfy Derby
    // MySQL uses LIMIT ... OFFSET ...
    // Others use window functions (more complex)
    if (paging.getFirstItemIndex() == 0) {
      return sqlSelectFromWhere + sqlOrderBy +
        "FETCH FIRST " + paging.getPagingSize() + " ROWS ONLY ";
    }
    return sqlSelectFromWhere + sqlOrderBy +
      "OFFSET " + paging.getFirstItemIndex() + " ROWS " +
      "FETCH FIRST " + paging.getPagingSize() + " ROWS ONLY ";
  }

  //-------------------------------------------------------------------------
  /**
   * Builds SQL to query a sequence (typically created with CREATE SEQUENCE).
   * @param sequenceName  the sequence name, not null
   * @return the SQL, not space terminated, not null
   */
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    // use SQL standard
    // works on Derby 10.6 onwards
    // SELECT NEXT VALUE FOR seq_name
    // Postgres uses SELECT nextval(seq_name)
    // Oracle uses SELECT seq_name.NEXTVAL FROM dual
    // Others use window functions (more complex)
    return "SELECT NEXT VALUE FOR " + sequenceName;
  }

  /**
   * Builds SQL to query a sequence (typically created with CREATE SEQUENCE).
   * @param sequenceName  the sequence name, not null
   * @return the SQL, space terminated, not null
   */
  public String sqlNextSequenceValueInline(final String sequenceName) {
    // use SQL standard
    // works on Derby 10.6 onwards
    // NEXT VALUE FOR seq_name
    // Postgres uses nextval(seq_name)
    // Oracle uses seq_name.NEXTVAL
    // Others use window functions (more complex)
    return "NEXT VALUE FOR " + sequenceName + " ";
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
