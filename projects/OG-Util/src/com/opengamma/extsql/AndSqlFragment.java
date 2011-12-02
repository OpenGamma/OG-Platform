/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of AND(expression).
 * <p>
 * This outputs an AND clause if the expression is true.
 * It also avoids outputting AND if the last thing in the buffer is WHERE.
 */
final class AndSqlFragment extends ConditionalSqlFragment {

  /**
   * Creates an instance.
   * 
   * @param variable  the variable to determine whether to include the AND on, not null
   * @param matchValue  the value to match, null to match on existence
   */
  AndSqlFragment(String variable, String matchValue) {
    super(variable, matchValue);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    if (isMatch(paramSource)) {
      if (endsWith(buf, " WHERE ") == false && endsWith(buf, " AND ") == false) {
        buf.append("AND ");
      }
      super.toSQL(buf, bundle, paramSource);
    }
  }

}
