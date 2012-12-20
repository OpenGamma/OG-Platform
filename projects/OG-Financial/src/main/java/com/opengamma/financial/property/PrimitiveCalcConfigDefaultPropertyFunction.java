/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import com.opengamma.engine.ComputationTargetType;

/**
 * Dummy function to inject default properties from the calculation configuration into the dependency graph for primitive targets.
 */
public abstract class PrimitiveCalcConfigDefaultPropertyFunction extends CalcConfigDefaultPropertyFunction {

  protected PrimitiveCalcConfigDefaultPropertyFunction(final boolean uniqueId) {
    super(ComputationTargetType.PRIMITIVE, uniqueId);
  }

  /**
   * Applies to any matching targets.
   */
  public static class Generic extends PrimitiveCalcConfigDefaultPropertyFunction {

    public Generic() {
      super(false);
    }

  }

  /**
   * Applies to specifically identified targets only.
   */
  public static class Specific extends PrimitiveCalcConfigDefaultPropertyFunction {

    public Specific() {
      super(true);
    }

  }

}
