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
  public static final String BOND_CLEAN_PRICE = "BondCleanPrice";
  /** Bond dirty price */
  public static final String BOND_DIRTY_PRICE = "BondDirtyPrice";
  /** Bond forward dirty price */
  public static final String BOND_FORWARD_DIRTY_PRICE = "BondForwardDirtyPrice";
  /** Bond clean price calculator */
  public static final BondCleanPriceCalculator BOND_CLEAN_PRICE_CALCULATOR = BondCleanPriceCalculator.getInstance();
  /** Bond dirty price calculator */
  public static final BondDirtyPriceCalculator BOND_DIRTY_PRICE_CALCULATOR = BondDirtyPriceCalculator.getInstance();
  /** Bond forward dirty price calculator */
  public static final BondForwardDirtyPriceCalculator BOND_FORWARD_DIRTY_PRICE_CALCULATOR = BondForwardDirtyPriceCalculator.getInstance();
  private static final Map<String, BondCalculator> s_bondInstances = new HashMap<String, BondCalculator>();
  private static final Map<Class<?>, String> s_bondInstanceNames = new HashMap<Class<?>, String>();
  private static final Map<String, BondForwardCalculator> s_bondForwardInstances = new HashMap<String, BondForwardCalculator>();
  private static final Map<Class<?>, String> s_bondForwardInstanceNames = new HashMap<Class<?>, String>();

  static {
    s_bondInstances.put(BOND_CLEAN_PRICE, BOND_CLEAN_PRICE_CALCULATOR);
    s_bondInstanceNames.put(BOND_CLEAN_PRICE_CALCULATOR.getClass(), BOND_CLEAN_PRICE);
    s_bondInstances.put(BOND_DIRTY_PRICE, BOND_DIRTY_PRICE_CALCULATOR);
    s_bondInstanceNames.put(BOND_DIRTY_PRICE_CALCULATOR.getClass(), BOND_DIRTY_PRICE);
    s_bondForwardInstances.put(BOND_FORWARD_DIRTY_PRICE, BOND_FORWARD_DIRTY_PRICE_CALCULATOR);
    s_bondForwardInstanceNames.put(BOND_FORWARD_DIRTY_PRICE_CALCULATOR.getClass(), BOND_FORWARD_DIRTY_PRICE);
  }

  private BondCalculatorFactory() {
  }

  public static BondCalculator getBondCalculator(final String name) {
    final BondCalculator calculator = s_bondInstances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getBondCalculatorName(final BondCalculator calculator) {
    if (calculator == null) {
      return null;
    }
    return s_bondInstanceNames.get(calculator.getClass());
  }

  public static BondForwardCalculator getBondForwardCalculator(final String name) {
    final BondForwardCalculator calculator = s_bondForwardInstances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator for " + name);
  }

  public static String getBondForwardCalculatorName(final BondForwardCalculator calculator) {
    if (calculator == null) {
      return null;
    }
    return s_bondForwardInstanceNames.get(calculator.getClass());
  }
}
