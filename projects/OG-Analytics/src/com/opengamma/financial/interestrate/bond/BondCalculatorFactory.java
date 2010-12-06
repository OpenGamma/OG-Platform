/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public final class BondCalculatorFactory {
  /** Bond clean price */
  public static final String CLEAN_PRICE = "CleanPrice";
  /** Bond dirty price */
  public static final String DIRTY_PRICE = "DirtyPrice";
  /** Bond clean price calculator */
  public static final BondCleanPriceCalculator CLEAN_PRICE_CALCULATOR = new BondCleanPriceCalculator();
  /** Bond dirty price calculator */
  public static final BondDirtyPriceCalculator DIRTY_PRICE_CALCULATOR = new BondDirtyPriceCalculator();
  private static final Map<String, BondCalculator> s_instances = new HashMap<String, BondCalculator>();
  private static final Map<Class<?>, String> s_instanceNames = new HashMap<Class<?>, String>();

  static {
    s_instances.put(CLEAN_PRICE, CLEAN_PRICE_CALCULATOR);
    s_instanceNames.put(CLEAN_PRICE_CALCULATOR.getClass(), CLEAN_PRICE);
    s_instances.put(DIRTY_PRICE, DIRTY_PRICE_CALCULATOR);
    s_instanceNames.put(DIRTY_PRICE_CALCULATOR.getClass(), DIRTY_PRICE);
  }

  private BondCalculatorFactory() {
  }

  public static BondCalculator getCalculator(final String name) {
    final BondCalculator calculator = s_instances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getCalculatorName(final BondCalculator calculator) {
    if (calculator == null) {
      return null;
    }
    return s_instanceNames.get(calculator.getClass());
  }

}
