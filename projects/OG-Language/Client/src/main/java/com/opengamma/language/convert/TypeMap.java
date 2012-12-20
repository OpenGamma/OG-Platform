/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.HashMap;

import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Helper for creating the costed conversion type map.
 */
public final class TypeMap extends HashMap<JavaTypeInfo<?>, Integer> {

  /**
   * No loss of precision; i.e. F-1 (F (x)) == x, although not always F (F-1 (x)) == x
   */
  public static final int ZERO_LOSS = 10;
  /**
   * No loss of precision, but an expensive or non-preferred operation.
   */
  public static final int ZERO_LOSS_NON_PREFERRED = 20;
  /**
   * Slight loss of precision.
   */
  public static final int MINOR_LOSS = 30;
  /**
   * Significant loss of precision.
   */
  public static final int MAJOR_LOSS = 50;

  /**
   * 
   */
  private static final long serialVersionUID = -780302044796660504L;

  private TypeMap() {
  }

  public TypeMap with(final int cost, final JavaTypeInfo<?>... types) {
    for (JavaTypeInfo<?> type : types) {
      put(type, cost);
    }
    return this;
  }

  public TypeMap withWeighted(int cost, final JavaTypeInfo<?>... types) {
    for (JavaTypeInfo<?> type : types) {
      put(type, cost++);
    }
    return this;
  }

  public TypeMap with(final int cost, final JavaTypeInfo<?> type) {
    put(type, cost);
    return this;
  }

  public static TypeMap builder() {
    return new TypeMap();
  }

  public static TypeMap of(final int cost, final JavaTypeInfo<?>... types) {
    return builder().with(cost, types);
  }

  public static TypeMap ofWeighted(final int cost, final JavaTypeInfo<?>... types) {
    return builder().withWeighted(cost, types);
  }

  public static TypeMap of(final int cost, final JavaTypeInfo<?> type) {
    return builder().with(cost, type);
  }

}
