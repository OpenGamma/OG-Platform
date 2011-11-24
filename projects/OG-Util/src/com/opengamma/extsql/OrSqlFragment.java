/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of OR(expression).
 * <p>
 * This outputs an OR clause if the expression is true.
 * It also avoids outputting OR if the last thing in the buffer is WHERE.
 */
public final class OrSqlFragment extends ConditionalSqlFragment {

  /**
   * Creates an instance.
   * 
   * @param variable  the variable to determine whether to include the OR on, not null
   * @param matchValue  the value to match, null to match on existence
   */
  public OrSqlFragment(String variable, String matchValue) {
    super(variable, matchValue);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    if (isMatch(paramSource)) {
      if (endsWith(buf, " WHERE ") == false && endsWith(buf, " OR ") == false) {
        buf.append("OR ");
      }
      super.toSQL(buf, bundle, paramSource);
    }
  }

}
