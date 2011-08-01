/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * State required for the current comparison operation. This may not be shared among multiple
 * comparisons, or by multiple threads.
 */
/* package */final class ComparisonContext {

  private final AbstractComparator _comparator;

  private final FudgeSerializationContext _fudgeSerializationContext;

  public ComparisonContext(final AbstractComparator comparator) {
    _comparator = comparator;
    _fudgeSerializationContext = new FudgeSerializationContext(comparator.getFudgeContext());
  }

  private AbstractComparator getComparator() {
    return _comparator;
  }

  public FudgeSerializationContext getFudgeSerializationContext() {
    return _fudgeSerializationContext;
  }

  public boolean isIgnorePositionAttributes() {
    return getComparator().isIgnorePositionAttributes();
  }

  public boolean isIgnoreTradeAttributes() {
    return getComparator().isIgnoreTradeAttributes();
  }

}
