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
 * Implementation of a provider of normal volatility (Bachelier model) smile for options on STIR futures. 
 * The volatility is time to expiration/simple price moneyness dependent (not strike).
 * The simple moneyness is computed as 
 *  - moneynessOnPrice = true: (strike price) - (futures price).
 *  - moneynessOnPrice = false: (futures price) - (strike price) = (1 - strike price) - (1 - futures price) = (strike rate) - (futures rate).
 */
public class NormalSTIRFuturesExpSimpleMoneynessProvider implements NormalSTIRFuturesProviderInterface {
  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurveProvider;
  /**
   * The normal volatility surface. Not null.
   */
  private final Surface<Double, Double, Double> _parameters;
  /** Flag indicating if the moneyness is on the price (true) or on the rate (false). */
  private final boolean _moneynessOnPrice;
  /**
   * The underlying swaps generators.
   */
  private final IborIndex _index;

  /**
   * Constructor.
   * @param multicurveProvider The multicurve provider.
   * @param parameters The normal volatility parameters.
   * @param index The index underlying the futures for which the date is valid.
   * @param moneynessOnPrice Flag indicating if the moneyness is on the price (true) or on the rate (false).
   */
  public NormalSTIRFuturesExpSimpleMoneynessProvider(final MulticurveProviderInterface multicurveProvider,
      final Surface<Double, Double, Double> parameters, final IborIndex index, final boolean moneynessOnPrice) {
    ArgumentChecker.notNull(multicurveProvider, "multicurveProvider");
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.notNull(index, "index");
    _multicurveProvider = multicurveProvider;
    _parameters = parameters;
    _index = index;
    _moneynessOnPrice = moneynessOnPrice;
  }

  @Override
  public NormalSTIRFuturesExpSimpleMoneynessProvider copy() {
    MulticurveProviderInterface multicurveProvider = _multicurveProvider.copy();
    return new NormalSTIRFuturesExpSimpleMoneynessProvider(multicurveProvider, _parameters, _index, _moneynessOnPrice);
  }

  @Override
  public double getVolatility(final double expiry, final double delay, final double strikePrice,
      final double futuresPrice) {
    double simpleMoneyness = _moneynessOnPrice ? strikePrice - futuresPrice : futuresPrice - strikePrice;
    return _parameters.getZValue(expiry, simpleMoneyness);
  }

  @Override
  public IborIndex getFuturesIndex() {
    return _index;
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurveProvider;
  }
  
  public boolean isMoneynessOnPrice() {
    return _moneynessOnPrice;
  }

  /**
   * Returns the Normal parameters.
   * @return The parameters.
   */
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
  public NormalSTIRFuturesProviderInterface withMulticurve(MulticurveProviderInterface multicurveProvider) {
    return new NormalSTIRFuturesExpSimpleMoneynessProvider(multicurveProvider, _parameters, _index, _moneynessOnPrice);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _index.hashCode();
    result = prime * result + (_moneynessOnPrice ? 1231 : 1237);
    result = prime * result +  _multicurveProvider.hashCode();
    result = prime * result +  _parameters.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NormalSTIRFuturesExpSimpleMoneynessProvider other = (NormalSTIRFuturesExpSimpleMoneynessProvider) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (_moneynessOnPrice != other._moneynessOnPrice) {
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
