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
 * @author jim
 */
public class DefaultDiscountCurveSource implements DiscountCurveSource {

  private Map<Pair<Currency, String>, DiscountCurveDefinition> _map;

  public DefaultDiscountCurveSource() {
    _map = new HashMap<Pair<Currency, String>, DiscountCurveDefinition>();
  }

  @Override
  public DiscountCurveDefinition getDefinition(Currency currency, String name) {
    return _map.get(Pair.of(currency, name));
  }

  public void addDefinition(Currency currency, String name, DiscountCurveDefinition definition) {
    _map.put(Pair.of(currency, name), definition);
  }
}
