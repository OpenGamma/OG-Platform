/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent.
 */
public class BlackBondFuturesSmilePriceProvider implements BlackBondFuturesSmilePriceProviderInterface {

  /**
   * The Black bond futures smile provider.
   */
  private final BlackBondFuturesSmileProviderInterface _blackProvider;
  /**
   * The underlying bond futures price.
   */
  private final double _price;

  /**
   * Constructor.
   * @param provider The curve and volatility surface provider, not null
   * @param price The underlying bond futures price.
   */
  public BlackBondFuturesSmilePriceProvider(final BlackBondFuturesSmileProviderInterface provider, final double price) {
    ArgumentChecker.notNull(provider, "provider");
    _blackProvider = provider;
    _price = price;
  }

  @Override
  public BlackBondFuturesSmilePriceProvider copy() {
    final BlackBondFuturesSmileProviderInterface blackProvider = _blackProvider.copy();
    return new BlackBondFuturesSmilePriceProvider(blackProvider, _price);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _blackProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _blackProvider.getIssuerProvider();
  }

  @Override
  public double getFuturesPrice() {
    return _price;
  }

  @Override
  public BlackBondFuturesSmileProviderInterface getBlackProvider() {
    return _blackProvider;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _blackProvider.getMulticurveProvider().parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _blackProvider.getMulticurveProvider().parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _blackProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _blackProvider.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackBondFuturesSmilePriceProvider)) {
      return false;
    }
    final BlackBondFuturesSmilePriceProvider other = (BlackBondFuturesSmilePriceProvider) obj;
    if (Double.compare(_price, other._price) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_blackProvider, other._blackProvider)) {
      return false;
    }
    return true;
  }

}
