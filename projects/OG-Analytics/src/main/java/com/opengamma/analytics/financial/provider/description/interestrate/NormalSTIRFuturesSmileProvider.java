/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of a provider of normal volatility (Bachelier model) smile for options on STIR futures. The volatility is time to expiration/strike/delay dependent.
 * The "delay" is the time between expiration of the option and last trading date of the underlying futures.
 */
public class NormalSTIRFuturesSmileProvider implements NormalSTIRFuturesProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The Black volatility cube. Not null.
   * TODO: Change to a cube (with the delay dimension).
   */
  private final Surface<Double, Double, Double> _parameters;
  /**
   * The underlying swaps generators.
   */
  private final IborIndex _index;

  /**
   * @param multicurveProvider The multicurve provider.
   * @param parameters The normal volatility parameters.
   * @param index The cap/floor index.
   */
  public NormalSTIRFuturesSmileProvider(final MulticurveProviderInterface multicurveProvider, final Surface<Double, Double, Double> parameters, final IborIndex index) {
    ArgumentChecker.notNull(multicurveProvider, "multicurveProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(index, "index");
    _multicurveProvider = multicurveProvider;
    _parameters = parameters;
    _index = index;
  }

  @Override
  public NormalSTIRFuturesSmileProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new NormalSTIRFuturesSmileProvider(multicurveProvider, _parameters, _index);
  }

  @Override
  public double getVolatility(final double expiry, final double delay, final double strike, final double priceFutures) {
    //TODO: delay is not used.
    return _parameters.getZValue(expiry, strike);
  }

  @Override
  public IborIndex getFuturesIndex() {
    return _index;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }

  @Override
  public Surface<Double, Double, Double> getNormalParameters() {
    return _parameters;
  }

  @Override
  public double[] parameterSensitivity(final String name, final List<DoublesPair> pointSensitivity) {
    return _multicurveProvider.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurveProvider.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurveProvider.getAllCurveNames();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + _multicurveProvider.hashCode();
    result = prime * result + _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NormalSTIRFuturesSmileProvider)) {
      return false;
    }
    final NormalSTIRFuturesSmileProvider other = (NormalSTIRFuturesSmileProvider) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_multicurveProvider, other._multicurveProvider)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

}
