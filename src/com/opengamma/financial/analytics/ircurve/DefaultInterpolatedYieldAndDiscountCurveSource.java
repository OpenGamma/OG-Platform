/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 *
 */
public class DefaultInterpolatedYieldAndDiscountCurveSource implements InterpolatedYieldAndDiscountCurveSource {

  private Map<Pair<Currency, String>, InterpolatedYieldAndDiscountCurveDefinition> _map;

  public DefaultInterpolatedYieldAndDiscountCurveSource() {
    _map = new HashMap<Pair<Currency, String>, InterpolatedYieldAndDiscountCurveDefinition>();
  }

  @Override
  public InterpolatedYieldAndDiscountCurveDefinition getDefinition(Currency currency, String name) {
    return _map.get(Pair.of(currency, name));
  }

  public void addDefinition(Currency currency, String name, InterpolatedYieldAndDiscountCurveDefinition definition) {
    _map.put(Pair.of(currency, name), definition);
  }
}
