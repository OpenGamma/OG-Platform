/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A group of functions whose outputs cannot be applied as inputs directly to any other functions in the group when operating on the same value requirement name. This is to work around inefficiencies
 * in the graph building algorithm when used with, for example, the default property function pattern used in OG-Financial. Not strictly part of a function definition, the exclusion groups have been
 * kept external in the same manner that function priorities are.
 */
public class FunctionExclusionGroup {

  private static final AtomicInteger s_nextId = new AtomicInteger();

  private final Object _key;
  private final String _displayName;

  /**
   * Creates a new exclusion group.
   * 
   * @param key a user key for use by the group comparison logic
   * @param displayName display name for diagnostic output
   */
  public FunctionExclusionGroup(final Object key, final String displayName) {
    _key = key;
    _displayName = displayName;
  }

  /**
   * Creates a new exclusion group.
   * 
   * @param displayName display name for diagnostic output
   */
  public FunctionExclusionGroup(final String displayName) {
    this(null, displayName);
  }

  /**
   * Creates a new exclusion group.
   */
  public FunctionExclusionGroup() {
    this("ExclusionGroup" + s_nextId.getAndIncrement());
  }

  protected String getDisplayName() {
    return _displayName;
  }

  protected Object getKey() {
    return _key;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

}
