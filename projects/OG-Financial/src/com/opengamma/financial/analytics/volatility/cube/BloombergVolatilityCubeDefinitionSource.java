/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashSet;
import java.util.Set;

import javax.time.InstantProvider;

import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Source of a single Volatility Cube Definition per currency: "BLOOMBERG" which includes all slices for which Bloomberg tickers exist
 */
public class BloombergVolatilityCubeDefinitionSource implements VolatilityCubeDefinitionSource {

  /**
   * The name of the definition which this source provides for all currencies
   */
  public static final String DEFINITION_NAME = "BLOOMBERG";

  private final BloombergSwaptionVolatilityCubeInstrumentProvider _instrumentProvider = BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG;

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name) {
    if (!DEFINITION_NAME.equals(name)) {
      return null;
    }
    final Set<Tenor> optionExpiries = new HashSet<Tenor>();
    final Set<Tenor> swapTenors = new HashSet<Tenor>();
    final Set<Double> relativeStrikes = new HashSet<Double>();

    final Set<VolatilityPoint> allPoints = _instrumentProvider.getAllPoints(currency);

    for (final VolatilityPoint volatilityPoint : allPoints) {
      optionExpiries.add(volatilityPoint.getOptionExpiry());
      swapTenors.add(volatilityPoint.getSwapTenor());
      relativeStrikes.add(volatilityPoint.getRelativeStrike());
    }

    final VolatilityCubeDefinition ret = new VolatilityCubeDefinition();
    ret.setOptionExpiries(Lists.newArrayList(optionExpiries));
    ret.setSwapTenors(Lists.newArrayList(swapTenors));
    ret.setRelativeStrikes(Lists.newArrayList(relativeStrikes));

    ret.setUniqueId(UniqueId.of("BLOOMBERG_VOLATILITY_CUBE_DEFINITION", currency.getCode()));
    return ret;
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final Currency currency, final String name, final InstantProvider version) {
    return getDefinition(currency, name);
  }

}
