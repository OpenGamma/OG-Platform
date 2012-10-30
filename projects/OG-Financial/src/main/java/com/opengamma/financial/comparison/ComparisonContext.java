/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import org.fudgemsg.mapping.FudgeSerializer;

/**
 * State required for the current comparison operation.
 * This may not be shared among multiple comparisons, or by multiple threads.
 */
/* package */final class ComparisonContext {

  private final AbstractComparator _comparator;

  private final FudgeSerializer _fudgeSerializer;

  public ComparisonContext(final AbstractComparator comparator) {
    _comparator = comparator;
    _fudgeSerializer = new FudgeSerializer(comparator.getFudgeContext());
  }

  private AbstractComparator getComparator() {
    return _comparator;
  }

  public FudgeSerializer getFudgeSerializer() {
    return _fudgeSerializer;
  }

  public boolean isIgnorePositionAttributes() {
    return getComparator().isIgnorePositionAttributes();
  }

  public boolean isIgnoreTradeAttributes() {
    return getComparator().isIgnoreTradeAttributes();
  }

}
