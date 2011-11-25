/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of INSERT(key).
 * <p>
 * This can insert another named SQL fragment or directly insert a parameter.
 */
public final class InsertSqlFragment extends SqlFragment {

  /**
   * The insert key.
   */
  private final String _insertKey;

  /**
   * Creates an instance.
   * 
   * @param insertKey  the insert key, not null
   */
  InsertSqlFragment(String insertKey) {
    if (insertKey == null) {
      throw new IllegalArgumentException("Insert key must be specified");
    }
    _insertKey = insertKey;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the insert key.
   * 
   * @return the insert key, not null
   */
  public String getInsertKey() {
    return _insertKey;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    if (_insertKey.startsWith(":")) {
      Object value = paramSource.getValue(_insertKey.substring(1));
      buf.append(value).append(' ');
    } else {
      NameSqlFragment unit = bundle.getFragment(_insertKey);
      unit.toSQL(buf, bundle, paramSource);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + _insertKey;
  }

}
