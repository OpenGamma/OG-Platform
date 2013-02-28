/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class VolatilityFunctionFactory {
  /** Standard Hagan  */
  public static final String HAGAN = "Hagan";
  /** Alternative Hagan  */
  public static final String ALTERNATIVE_HAGAN = "Alternative Hagan";
  /** Berestycki  */
  public static final String BERESTYCKI = "Berestycki";
  /** Johnson  */
  public static final String JOHNSON = "Johnson";
  /** Paulot  */
  public static final String PAULOT = "Paulot";
  /** Standard Hagan formula */
  public static final SABRHaganVolatilityFunction HAGAN_FORMULA = new SABRHaganVolatilityFunction();
  /** Alternative Hagan formula */
  public static final SABRHaganAlternativeVolatilityFunction ALTERNATIVE_HAGAN_FORMULA = new SABRHaganAlternativeVolatilityFunction();
  /** Berestycki formula */
  public static final SABRBerestyckiVolatilityFunction BERESTYCKI_FORMULA = new SABRBerestyckiVolatilityFunction();
  /** Johnson formula */
  public static final SABRJohnsonVolatilityFunction JOHNSON_FORMULA = new SABRJohnsonVolatilityFunction();
  /** Paulot formula */
  public static final SABRPaulotVolatilityFunction PAULOT_FORMULA = new SABRPaulotVolatilityFunction();

  private static final Map<String, VolatilityFunctionProvider<?>> s_instances = new HashMap<>();
  private static final Map<Class<? extends VolatilityFunctionProvider<?>>, String> s_instanceNames = new HashMap<>();

  static {
    s_instances.put(ALTERNATIVE_HAGAN, ALTERNATIVE_HAGAN_FORMULA);
    s_instances.put(BERESTYCKI, BERESTYCKI_FORMULA);
    s_instances.put(HAGAN, HAGAN_FORMULA);
    s_instances.put(JOHNSON, JOHNSON_FORMULA);
    s_instances.put(PAULOT, PAULOT_FORMULA);
    s_instanceNames.put(ALTERNATIVE_HAGAN_FORMULA.getClass(), ALTERNATIVE_HAGAN);
    s_instanceNames.put(BERESTYCKI_FORMULA.getClass(), BERESTYCKI);
    s_instanceNames.put(HAGAN_FORMULA.getClass(), HAGAN);
    s_instanceNames.put(JOHNSON_FORMULA.getClass(), JOHNSON);
    s_instanceNames.put(PAULOT_FORMULA.getClass(), PAULOT);
  }

  private VolatilityFunctionFactory() {
  }

  public static VolatilityFunctionProvider<?> getCalculator(final String name) {
    final VolatilityFunctionProvider<?> calculator = s_instances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getCalculatorName(final VolatilityFunctionProvider<?> calculator) {
    if (calculator == null) {
      return null;
    }
    return s_instanceNames.get(calculator.getClass());
  }

}
