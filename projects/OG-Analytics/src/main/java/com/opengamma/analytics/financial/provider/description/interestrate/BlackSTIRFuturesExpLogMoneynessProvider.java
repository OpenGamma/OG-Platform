/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Implementation of a provider of Black smile for options on bond futures. The volatility is time to expiration/log-moneyness dependent.
 * The delay is the time difference between the last notice and the option expiration.
 * The log-moneyness is computed as log(1-K/1-F) where K is the strike price and F the futures price, i.e. the ratio of strike rate and futures rate.
 */
public class BlackSTIRFuturesExpLogMoneynessProvider implements BlackSTIRFuturesProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurve;
  /**
   * The Black volatility surface. Not null. The dimensions are expiration and log moneyness (ln(1.0-strikePrice)/(1.0-futuresPrice)).
   */
  private final Surface<Double, Double, Double> _parameters;
  /**
   * The Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index.
   */
  private final IborIndex _index;

  /**
   * Constructor.
   * @param multicurve The multicurve provider.
   * @param parameters The Black parameters, not null
   * @param index The Ibor Index of the futures on for which the Black data is valid.
   */
  public BlackSTIRFuturesExpLogMoneynessProvider(final MulticurveProviderInterface multicurve, final Surface<Double, Double, Double> parameters,
      final IborIndex index) {
    ArgumentChecker.notNull(multicurve, "multicurve");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(index, "Ibor index");
    _multicurve = multicurve;
    _parameters = parameters;
    _index = index;
  }

  @Override
  public BlackSTIRFuturesExpLogMoneynessProvider copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurve.copy();
    return new BlackSTIRFuturesExpLogMoneynessProvider(multicurveProvider, _parameters, _index);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurve;
  }

  @Override
  /**
   * Gets the Black volatility at a given expiry-delay point. The strike dimension is ignored.
   * @param expiry The time to expiration.
   * @param delay The delay between the option expiry and the futures expiry.
   * @param strike The option strike. Dimension ignored.
   * @param futuresPrice The price of the underlying futures. Dimension ignored.
   * @return The volatility.
   */
  public double getVolatility(final double expiry, final double delay, final double strikePrice, final double futuresPrice) {
    ArgumentChecker.isTrue(futuresPrice < 1.0d, "futures price above 1.0");
    ArgumentChecker.isTrue(strikePrice < 1.0d, "strike price above 1.0");
    final double logMoneyness = Math.log((1.0d - strikePrice) / (1.0d - futuresPrice));
    return _parameters.getZValue(expiry, logMoneyness);
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
    return _multicurve.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(final String name, final List<ForwardSensitivity> pointSensitivity) {
    return _multicurve.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurve.getAllCurveNames();
  }

  @Override
  public IborIndex getFuturesIndex() {
    return _index;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _multicurve.hashCode();
    result = prime * result + _parameters.hashCode();
    result = prime * result + _index.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackSTIRFuturesExpLogMoneynessProvider)) {
      return false;
    }
    final BlackSTIRFuturesExpLogMoneynessProvider other = (BlackSTIRFuturesExpLogMoneynessProvider) obj;
    if (!ObjectUtils.equals(_multicurve, other._multicurve)) {
      return false;
    }
    if (!ObjectUtils.equals(_parameters, other._parameters)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
