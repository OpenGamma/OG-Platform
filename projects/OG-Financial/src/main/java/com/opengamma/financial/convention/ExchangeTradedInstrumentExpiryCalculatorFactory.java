/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ExchangeTradedInstrumentExpiryCalculatorFactory {
  private static final Map<String, ExchangeTradedInstrumentExpiryCalculator> s_instances = new HashMap<String, ExchangeTradedInstrumentExpiryCalculator>();

  static {
    s_instances.put(BondFutureOptionExpiryCalculator.NAME, BondFutureOptionExpiryCalculator.getInstance());
    s_instances.put(IMMFutureAndFutureOptionQuarterlyExpiryCalculator.NAME, IMMFutureAndFutureOptionQuarterlyExpiryCalculator.getInstance());
    s_instances.put(IMMFutureAndFutureOptionMonthlyExpiryCalculator.NAME, IMMFutureAndFutureOptionMonthlyExpiryCalculator.getInstance());
    s_instances.put(SoybeanFutureExpiryCalculator.NAME, SoybeanFutureExpiryCalculator.getInstance());
    s_instances.put(SoybeanFutureOptionExpiryCalculator.NAME, SoybeanFutureOptionExpiryCalculator.getInstance());
  }

  public static ExchangeTradedInstrumentExpiryCalculator getCalculator(final String name) {
    final ExchangeTradedInstrumentExpiryCalculator calculator = s_instances.get(name);
    if (calculator != null) {
      return calculator;
    }
    throw new IllegalArgumentException("Could not get calculator called " + name);
  }

}
