/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
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
  public VolatilityCubeDefinition<Tenor, Tenor, Double> getDefinition(final String name, final String instrumentType) {
    if (!DEFINITION_NAME.equals(name)) {
      return null;
    }
    final Set<Tenor> optionExpiries = new HashSet<>();
    final Set<Tenor> swapTenors = new HashSet<>();
    final Set<Double> relativeStrikes = new HashSet<>();

    final Set<VolatilityPoint> allPoints = _instrumentProvider.getAllPoints(currency);

    for (final VolatilityPoint volatilityPoint : allPoints) {
      optionExpiries.add(volatilityPoint.getOptionExpiry());
      swapTenors.add(volatilityPoint.getSwapTenor());
      relativeStrikes.add(volatilityPoint.getRelativeStrike());
    }

    Tenor[] maturities = optionExpiries.toArray(new Tenor[0]);
    Tenor[] expires = swapTenors.toArray(new Tenor[0]);
    Double[] strikes = relativeStrikes.toArray(new Double[0]);
    UniqueId uid = UniqueId.of("BLOOMBERG_VOLATILITY_CUBE_DEFINITION", currency.getCode());
    return new VolatilityCubeDefinition<>(name, uid, maturities, expires, strikes);
  }

  @Override
  public VolatilityCubeDefinition getDefinition(final String name, final String instrumentType, final VersionCorrection versionCorrection) {
    return getDefinition(name, instrumentType);
  }

}
