/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of WHERE.
 * <p>
 * This outputs a WHERE clause if at least one child was output.
 */
final class WhereSqlFragment extends ContainerSqlFragment {

  /**
   * Creates an instance.
   */
  WhereSqlFragment() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    int oldLen = buf.length();
    buf.append("WHERE ");
    int newLen = buf.length();
    super.toSQL(buf, bundle, paramSource);
    if (buf.length() == newLen) {
      buf.setLength(oldLen);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + getFragments();
  }

}
