/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.elsql;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of paging over an SQL clause.
 */
final class PagingSqlFragment extends ContainerSqlFragment {

  /**
   * The offset variable.
   */
  private final String _offsetVariable;
  /**
   * The fetch variable or numeric amount.
   */
  private final String _fetchVariable;

  /**
   * Creates an instance.
   * 
   * @param offsetVariable  the offset variable, not null
   * @param fetchVariable  the fetch variable, not null
   */
  PagingSqlFragment(String offsetVariable, String fetchVariable) {
    _offsetVariable = offsetVariable;
    _fetchVariable = fetchVariable;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ElSqlBundle bundle, SqlParameterSource paramSource) {
    int oldLen = buf.length();
    super.toSQL(buf, bundle, paramSource);
    int newLen = buf.length();
    String select = buf.substring(oldLen, newLen);
    if (select.startsWith("SELECT ")) {
      buf.setLength(oldLen);
      buf.append(applyPaging(select, bundle, paramSource));
    }
  }

  /**
   * Applies the paging.
   * 
   * @param selectToPage  the contents of the enclosed block, not null
   * @param bundle  the elsql bundle for context, not null
   * @param paramSource  the SQL parameters, not null
   */
  protected String applyPaging(String selectToPage, ElSqlBundle bundle, SqlParameterSource paramSource) {
    int offset = 0;
    int fetchLimit = 0;
    if (_offsetVariable != null && paramSource.hasValue(_offsetVariable)) {
      offset = ((Number) paramSource.getValue(_offsetVariable)).intValue();
    }
    if (paramSource.hasValue(_fetchVariable)) {
      fetchLimit = ((Number) paramSource.getValue(_fetchVariable)).intValue();
    } else if (StringUtils.containsOnly(_fetchVariable, "0123456789")) {
      fetchLimit = Integer.parseInt(_fetchVariable);
    }
    return bundle.getConfig().addPaging(selectToPage, offset, fetchLimit == Integer.MAX_VALUE ? 0 : fetchLimit);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + getFragments();
  }

}
