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
 *
 */
//TODO make this a Bean
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

  public CarrLeeFXData(final Pair<Currency, Currency> currencyPair, final SmileDeltaTermStructureParameters volatilitySurface, final MulticurveProviderInterface curves) {
    ArgumentChecker.notNull(currencyPair, "currencyPair");
    ArgumentChecker.notNull(volatilitySurface, "volatilitySurface");
    ArgumentChecker.notNull(curves, "curves");
    _currencyPair = currencyPair;
    _volatilitySurface = volatilitySurface;
    _curves = curves;
    _realizedVariance = null;
  }

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
  public SmileDeltaTermStructureParameters getVolatilitySurface() {
    return _volatilitySurface;
  }

  /**
   * Returns volatility for a expiration, strike and forward. The volatility take into account the currency order.
   * @param ccy1 The first currency.
   * @param ccy2 The second currency.
   * @param time The expiration time.
   * @param strike The strike.
   * @param forward The forward rate.
   * @return The volatility.
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
  public double getRealizedVariance() {
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
