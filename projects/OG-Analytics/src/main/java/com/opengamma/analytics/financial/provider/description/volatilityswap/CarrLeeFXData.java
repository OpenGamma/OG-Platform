/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.volatilityswap;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.volatility.VolatilityAndBucketedSensitivities;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Contains the information required to price FX volatility swaps using the Carr-Lee model.
 */
//TODO make this a Bean when curve provider and volatility surface are beans.
public class CarrLeeFXData implements CarrLeeData<MulticurveProviderInterface, SmileDeltaTermStructureParameters> {

  /**
   * The FX volatility data.
   */
  private final SmileDeltaTermStructureParameters _volatilitySurface;

  /**
   * The curves.
   */
  private final MulticurveProviderInterface _curves;

  /**
   * The realized variance. May be null.
   */
  private final Double _realizedVariance;

  /**
   * The currency pair.
   */
  private final Pair<Currency, Currency> _currencyPair;

  /**
   * Sets the realized variance to null. This constructor should not be used when attempting to
   * price seasoned swaps.
   * @param currencyPair The currency pair for which the data apply, not null
   * @param volatilitySurface The volatility surface, not null
   * @param curves The curves, not null
   */
  public CarrLeeFXData(final Pair<Currency, Currency> currencyPair, final SmileDeltaTermStructureParameters volatilitySurface, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(currencyPair, "currencyPair");
    ArgumentChecker.notNull(volatilitySurface, "volatilitySurface");
    ArgumentChecker.notNull(curves, "curves");
    _currencyPair = currencyPair;
    _volatilitySurface = volatilitySurface;
    _curves = curves;
    _realizedVariance = null;
  }

  /**
   * @param currencyPair The currency pair for which the data apply, not null
   * @param volatilitySurface The volatility surface, not null
   * @param curves The curves, not null
   * @param realizedVariance The realized variance, not null
   */
  public CarrLeeFXData(final Pair<Currency, Currency> currencyPair, final SmileDeltaTermStructureParameters volatilitySurface, final MulticurveProviderInterface curves,
      final Double realizedVariance) {
    ArgumentChecker.notNull(currencyPair, "currencyPair");
    ArgumentChecker.notNull(volatilitySurface, "volatilitySurface");
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(realizedVariance, "realizedVariance");
    _currencyPair = currencyPair;
    _volatilitySurface = volatilitySurface;
    _curves = curves;
    _realizedVariance = realizedVariance;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _curves;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _curves.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _curves.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _curves.getAllCurveNames();
  }

  @Override
  public CarrLeeData<MulticurveProviderInterface, SmileDeltaTermStructureParameters> copy() {
    final MulticurveProviderInterface curves = _curves.copy();
    return new CarrLeeFXData(_currencyPair, _volatilitySurface, curves);
  }

  @Override
  public SmileDeltaTermStructureParameters getVolatilityData() {
    return _volatilitySurface;
  }

  /**
   * Returns volatility for a time, strike and forward. The volatility surface takes into account the currency order.
   * @param ccy1 The first currency
   * @param ccy2 The second currency
   * @param time The time to expiry
   * @param strike The strike
   * @param forward The forward FX rate
   * @return The volatility
   */
  public double getVolatility(final Currency ccy1, final Currency ccy2, final double time, final double strike, final double forward) {
    if (_currencyPair.getFirst().equals(ccy1) && _currencyPair.getSecond().equals(ccy2)) {
      return _volatilitySurface.getVolatility(Triple.of(time, strike, forward));
    }
    if (_currencyPair.getFirst().equals(ccy2) && _currencyPair.getSecond().equals(ccy1)) {
      return _volatilitySurface.getVolatility(Triple.of(time, 1.0 / strike, 1.0 / forward));
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  /**
   * Returns the volatility and bucketed sensitivities for a time, strike and forward. The volatility surface takes into
   * account the currency order.
   * @param ccy1 The first currency
   * @param ccy2 The second currency
   * @param time The time to expiry
   * @param strike The strike
   * @param forward The forward
   * @return The volatilty
   */
  public VolatilityAndBucketedSensitivities getVolatilityAndSensitivities(final Currency ccy1, final Currency ccy2, final double time,
      final double strike, final double forward) {
    if (_currencyPair.getFirst().equals(ccy1) && _currencyPair.getSecond().equals(ccy2)) {
      return _volatilitySurface.getVolatilityAndSensitivities(Triple.of(time, strike, forward));
    }
    if (_currencyPair.getFirst().equals(ccy2) && _currencyPair.getSecond().equals(ccy1)) {
      return _volatilitySurface.getVolatilityAndSensitivities(Triple.of(time, 1.0 / strike, 1.0 / forward));
    }
    throw new IllegalArgumentException("Currencies not compatible with smile data; asked for " + ccy1 + " and " + ccy2);
  }

  @Override
  public double getSpot() {
    return _curves.getFxRate(_currencyPair.getFirst(), _currencyPair.getSecond());
  }

  @Override
  public Double getRealizedVariance() {
    return _realizedVariance;
  }

  /**
   * Gets the currency pair.
   * @return the currency pair
   */
  public Pair<Currency, Currency> getCurrencyPair() {
    return _currencyPair;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currencyPair.hashCode();
    result = prime * result + _curves.hashCode();
    result = prime * result + _realizedVariance.hashCode();
    result = prime * result + _volatilitySurface.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CarrLeeFXData)) {
      return false;
    }
    final CarrLeeFXData other = (CarrLeeFXData) obj;
    if (Double.compare(_realizedVariance, other._realizedVariance) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_currencyPair, other._currencyPair)) {
      return false;
    }
    if (!ObjectUtils.equals(_curves, other._curves)) {
      return false;
    }
    if (!ObjectUtils.equals(_volatilitySurface, other._volatilitySurface)) {
      return false;
    }
    return true;
  }

}
