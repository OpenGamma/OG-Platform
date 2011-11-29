/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of LIKE(variable).
 * <p>
 * This handles switching between LIKE and = based on the presence of wildcards.
 */
final class LikeSqlFragment extends ContainerSqlFragment {

  /**
   * The variable.
   */
  private final String _variable;

  /**
   * Creates an instance.
   * 
   * @param variable  the variable to base the LIKE on, not null
   */
  LikeSqlFragment(String variable) {
    if (variable == null) {
      throw new IllegalArgumentException("Variable must be specified");
    }
    if (variable.startsWith(":") == false || variable.length() < 2) {
      throw new IllegalArgumentException("Argument is not a variable (starting with a colon)");
    }
    _variable = variable.substring(1);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the variable.
   * 
   * @return the variable, not null
   */
  public String getVariable() {
    return _variable;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    String value = ObjectUtils.toString(paramSource.getValue(_variable));
    if (bundle.getConfig().isLikeWildcard(value)) {
      buf.append("LIKE ");
      super.toSQL(buf, bundle, paramSource);
      buf.append(bundle.getConfig().getLikeSuffix());
    } else {
      buf.append("= ");
      super.toSQL(buf, bundle, paramSource);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + _variable + " " + getFragments();
  }

}
