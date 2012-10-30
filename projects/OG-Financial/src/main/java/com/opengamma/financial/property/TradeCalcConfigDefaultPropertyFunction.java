/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import com.opengamma.engine.ComputationTargetType;

/**
 * Dummy function to inject default properties from the calculation configuration into the dependency graph for trade targets.
 */
public abstract class TradeCalcConfigDefaultPropertyFunction extends CalcConfigDefaultPropertyFunction {

  protected TradeCalcConfigDefaultPropertyFunction(final boolean identifier) {
    super(ComputationTargetType.TRADE, identifier);
  }

  /**
   * Applies to any matching targets.
   */
  public static class Generic extends TradeCalcConfigDefaultPropertyFunction {

    public Generic() {
      super(false);
    }

  }

  /**
   * Applies to specifically identified targets only.
   */
  public static class Specific extends TradeCalcConfigDefaultPropertyFunction {

    public Specific() {
      super(true);
    }

  }

}
