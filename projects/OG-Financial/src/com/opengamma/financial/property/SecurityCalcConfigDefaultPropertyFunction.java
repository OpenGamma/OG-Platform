/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import com.opengamma.engine.ComputationTargetType;

/**
 * Dummy function to inject default properties from the calculation configuration into the dependency graph for security targets.
 */
public abstract class SecurityCalcConfigDefaultPropertyFunction extends CalcConfigDefaultPropertyFunction {

  protected SecurityCalcConfigDefaultPropertyFunction(final boolean uniqueId) {
    super(ComputationTargetType.SECURITY, uniqueId);
  }

  /**
   * Applies to any matching targets.
   */
  public static class Generic extends SecurityCalcConfigDefaultPropertyFunction {

    public Generic() {
      super(false);
    }

  }

  /**
   * Applies to specifically identified targets only.
   */
  public static class Specific extends SecurityCalcConfigDefaultPropertyFunction {

    public Specific() {
      super(true);
    }

  }

}
