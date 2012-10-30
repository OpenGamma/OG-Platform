/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.comparison;

import org.fudgemsg.FudgeContext;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class of comparison operation providers. Contains any additional state to control a
 * comparison.
 */
/* package */abstract class AbstractComparator {

  /**
   * A Fudge context for extracting security information.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Whether to ignore trade attributes when making comparisons.
   */
  private boolean _ignoreTradeAttributes;

  /**
   * Whether to ignore position attributes when making comparisons.
   */
  private boolean _ignorePositionAttributes;

  // TODO: other parameters that will determine a "change" event from just a mismatch

  protected AbstractComparator(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setIgnorePositionAttributes(final boolean ignorePositionAttributes) {
    _ignorePositionAttributes = ignorePositionAttributes;
  }

  public boolean isIgnorePositionAttributes() {
    return _ignorePositionAttributes;
  }

  public void setIgnoreTradeAttributes(final boolean ignoreTradeAttributes) {
    _ignoreTradeAttributes = ignoreTradeAttributes;
  }

  public boolean isIgnoreTradeAttributes() {
    return _ignoreTradeAttributes;
  }

  protected ComparisonContext createContext() {
    return new ComparisonContext(this);
  }

}
