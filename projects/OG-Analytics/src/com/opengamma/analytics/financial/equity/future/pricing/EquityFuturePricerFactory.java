/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory of EquityFuture pricing methods
 */
public final class EquityFuturePricerFactory {

  private EquityFuturePricerFactory() {
  }

  /** Pricing via published (or computed) market futures price. See {@link EquityFutureMarkToMarket}*/
  public static final String MARK_TO_MARKET = "MARK_TO_MARKET";
  /** EquityFutureMarkToMarket instance */
  public static final EquityFutureMarkToMarket MARK_TO_MARKET_INSTANCE = EquityFutureMarkToMarket.getInstance();

  /** Pricing given the spot underlying and a scalar cost of Carry.  See {@link EquityFutureCostOfCarry}*/
  public static final String COST_OF_CARRY = "COST_OF_CARRY";
  /** EquityFutureCostOfCarry instance */
  public static final EquityFutureCostOfCarry COST_OF_CARRY_INSTANCE = EquityFutureCostOfCarry.getInstance();

  /** Pricing from a spot underlying asset, a scalar deterministic continuous dividend yield of the asset, and a YieldAndDiscountCurve */
  public static final String DIVIDEND_YIELD = "DIVIDEND_YIELD";
  /** EquityFutureDividendYield instance */
  public static final EquityFutureDividendYield DIVIDEND_YIELD_INSTANCE = EquityFutureDividendYield.getInstance();

  private static final Map<String, EquityFuturesPricer> s_staticInstances;
  private static final Map<Class<?>, String> s_instanceNames;

  static {
    final Map<String, EquityFuturesPricer> staticInstances = new HashMap<String, EquityFuturesPricer>();
    final Map<Class<?>, String> instanceNames = new HashMap<Class<?>, String>();
    staticInstances.put(MARK_TO_MARKET, MARK_TO_MARKET_INSTANCE);
    instanceNames.put(EquityFutureMarkToMarket.class, MARK_TO_MARKET);
    staticInstances.put(COST_OF_CARRY, COST_OF_CARRY_INSTANCE);
    instanceNames.put(EquityFutureCostOfCarry.class, COST_OF_CARRY);
    staticInstances.put(DIVIDEND_YIELD, DIVIDEND_YIELD_INSTANCE);
    instanceNames.put(EquityFutureDividendYield.class, DIVIDEND_YIELD);

    s_staticInstances = new HashMap<String, EquityFuturesPricer>(staticInstances);
    s_instanceNames = new HashMap<Class<?>, String>(instanceNames);
  }

  public static EquityFuturesPricer getMethod(final String methodName) {
    final EquityFuturesPricer pricer = s_staticInstances.get(methodName);
    if (pricer != null) {
      return pricer;
    }
    throw new IllegalArgumentException("Pricing method not handled: " + methodName);
  }

  public static String getPricerName(final EquityFuturesPricer pricer) {
    if (pricer == null) {
      return null;
    }
    final String pricerName = s_instanceNames.get(pricer.getClass());
    return pricerName;
  }

}
