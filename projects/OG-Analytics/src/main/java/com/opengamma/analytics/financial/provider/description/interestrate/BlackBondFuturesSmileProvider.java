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
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/strike dependent.
 */
public class BlackBondFuturesSmileProvider implements BlackBondFuturesSmileProviderInterface {

  /**
   * The multicurve provider.
   */
  private final IssuerProviderInterface _issuerProvider;
  /**
   * The Black volatility surface. Not null. The dimensions are expiry and strike.
   */
  private final Surface<Double, Double, Double> _parameters;

  /**
   * Constructor.
   * @param issuerProvider The issuer and multi-curve provider, not null
   * @param parameters The Black parameters, not null
   */
  public BlackBondFuturesSmileProvider(final IssuerProviderInterface issuerProvider, final Surface<Double, Double, Double> parameters) {
    ArgumentChecker.notNull(issuerProvider, "issuerProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    _issuerProvider = issuerProvider;
    _parameters = parameters;
  }

  @Override
  public BlackBondFuturesSmileProvider copy() {
    final IssuerProviderInterface multicurveProvider = _issuerProvider.copy();
    return new BlackBondFuturesSmileProvider(multicurveProvider, _parameters);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _issuerProvider.getMulticurveProvider();
  }

  @Override
  public IssuerProviderInterface getIssuerProvider() {
    return _issuerProvider;
  }

  @Override
  public double getVolatility(final double expiry, final double strike) {
    return _parameters.getZValue(expiry, strike);
  }

  /**
   * Returns the Black parameters.
   * @return The parameters.
   */
  public Surface<Double, Double, Double> getBlackParameters() {
    return _parameters;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _issuerProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _issuerProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _issuerProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _issuerProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackBondFuturesSmileProvider)) {
      return false;
    }
    final BlackBondFuturesSmileProvider other = (BlackBondFuturesSmileProvider) obj;
    if (!ObjectUtils.equals(_issuerProvider, other._issuerProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
