/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import com.opengamma.engine.ComputationTargetType;

/**
 * Injects a default currency requirement into the graph at a security.
 */
public abstract class SecurityDefaultCurrencyFunction extends DefaultCurrencyFunction {

  protected SecurityDefaultCurrencyFunction(final boolean permitWithout, final String valueName) {
    super(ComputationTargetType.SECURITY, permitWithout, valueName);
  }

  protected SecurityDefaultCurrencyFunction(final boolean permitWithout, final String... valueNames) {
    super(ComputationTargetType.SECURITY, permitWithout, valueNames);
  }

  /**
   * Flagged to inject the default currency at a higher priority to avoid other functions
   * handling the currency omitted case. 
   */
  public static class Strict extends SecurityDefaultCurrencyFunction {

    public Strict(final String valueName) {
      super(false, valueName);
    }

    public Strict(final String... valueNames) {
      super(false, valueNames);
    }

  }

  /**
   * Flagged to inject the default currency at a lower priority to allow other functions to
   * handle the currency omitted case.
   */
  public static class Permissive extends SecurityDefaultCurrencyFunction {

    public Permissive(final String valueName) {
      super(true, valueName);
    }

    public Permissive(final String... valueNames) {
      super(true, valueNames);
    }

  }

}
