/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of conditional SQL fragment.
 */
public abstract class ConditionalSqlFragment extends ContainerSqlFragment {

  /**
   * The variable.
   */
  private final String _variable;
  /**
   * The value to match against.
   */
  private final String _matchValue;

  /**
   * Creates an instance.
   * 
   * @param variable  the variable to determine whether to include the AND on, not null
   * @param matchValue  the value to match, null to match on existence
   */
  public ConditionalSqlFragment(String variable, String matchValue) {
    if (variable == null) {
      throw new IllegalArgumentException("Variable must be specified");
    }
    if (variable.startsWith(":") == false || variable.length() < 2) {
      throw new IllegalArgumentException("Argument is not a variable (starting with a colon)");
    }
    _variable = variable.substring(1);
    _matchValue = matchValue;
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

  /**
   * Gets the match value.
   * 
   * @return the match value, not null
   */
  public String getMatchValue() {
    return _matchValue;
  }

  //-------------------------------------------------------------------------
  protected boolean isMatch(SqlParameterSource paramSource) {
    if (paramSource.hasValue(_variable) == false) {
      return false;
    }
    Object value = paramSource.getValue(_variable);
    if (_matchValue != null) {
      return _matchValue.equalsIgnoreCase(value.toString());
    }
    if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue();
    }
    return true;
  }

  protected boolean endsWith(StringBuilder buf, String match) {
    String str = (buf.length() >= match.length() ? buf.substring(buf.length() - match.length()) : "");
    return str.equals(match);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + _variable + " " + getFragments();
  }

}
