/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;


/**
 * A group of functions whose outputs cannot be applied as inputs directly, or indirectly, to any other functions in the group. This is to work around inefficiencies in the graph building algorithm
 * when used with, for example, the default property function pattern used in OG-Financial. Not strictly part of a function definition, the exclusion groups have been kept external in the same manner
 * that function priorities are.
 */
public final class FunctionExclusionGroup {

  private final String _displayName;

  /**
   * Creates a new exclusion group.
   * 
   * @param displayName display name for diagnostic output
   */
  public FunctionExclusionGroup(final String displayName) {
    _displayName = displayName;
  }

  /**
   * Creates a new exclusion group.
   */
  public FunctionExclusionGroup() {
    this(null);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if (_displayName != null) {
      sb.append(_displayName);
    } else {
      sb.append("ExclusionGroup");
    }
    sb.append(hashCode());
    return sb.toString();
  }

}
