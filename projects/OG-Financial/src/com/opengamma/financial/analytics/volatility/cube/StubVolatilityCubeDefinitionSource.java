/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import javax.time.Instant;
import javax.time.InstantProvider;
import javax.time.calendar.Period;

import com.google.common.collect.Lists;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * TODO: this
 */
public class StubVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  @Override
  public VolatilityCubeDefinition getDefinition(Currency currency, String name) {
    return getDefinition(currency, name, Instant.now());
  }

  @Override
  public VolatilityCubeDefinition getDefinition(Currency currency, String name, InstantProvider version) {
    if ("TEST".equals(name)) {
      VolatilityCubeDefinition volatilityCubeDefinition = new VolatilityCubeDefinition();
      volatilityCubeDefinition.setSwapTenors(Lists.newArrayList(Tenor.ONE_YEAR, new Tenor(Period.ofYears(10))));
      volatilityCubeDefinition.setOptionExpiries(Lists.newArrayList(Tenor.ONE_MONTH, Tenor.TWELVE_MONTHS));
      volatilityCubeDefinition.setRelativeStrikes(Lists.newArrayList(Double.valueOf(-100), Double.valueOf(0), Double.valueOf(100)));
      return volatilityCubeDefinition;
    }
    return null;
  }

}
