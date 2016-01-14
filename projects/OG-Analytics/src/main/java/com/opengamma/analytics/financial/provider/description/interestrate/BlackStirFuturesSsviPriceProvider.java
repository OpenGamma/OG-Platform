/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.model.volatility.smile.function.SSVIVolatilityFunction;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Implementation of Black implied volatility for STIR futures with volatility given by a SSVI formula.
 */
public class BlackStirFuturesSsviPriceProvider implements BlackSTIRFuturesProviderInterface {

  /**
   * The multicurve provider.
   */
  private final MulticurveProviderInterface _multicurve;
  /**
   * The Black ATM implied volatility curve.
   */
  private final DoublesCurve _volatilityAtm;
  /** The rho parameter of the SSVI formula. **/
  private final double _rho;
  /** The eta parameter of the SSVI formula. **/
  private final double _eta;
  /**
   * The Ibor Index of the futures on for which the Black data is valid, i.e. the data is calibrated to futures on the given index.
   */
  private final IborIndex _index;

  /**
   * Constructor.
   * @param multicurve The multi-curve provider.
   * @param volatilityAtm The ATM imoplied volatility curve.
   * @param rho The rho parameter.
   * @param eta The eta parameter.
   * @param index The Ibor Index of the futures on for which the Black data is valid.
   */
  public BlackStirFuturesSsviPriceProvider(
      MulticurveProviderInterface multicurve,
      DoublesCurve volatilityAtm,
      double rho,
      double eta,
      IborIndex index) {
    this._multicurve = multicurve;
    this._volatilityAtm = volatilityAtm;
    this._rho = rho;
    this._eta = eta;
    this._index = index;
  }
  
  /**
   * Computes the volatility and its derivative with respect to the inputs.
   * @param expiry The option time to expiration.
   * @param delay The delay between expiration of the option and last trading date of the underlying futures.
   * @param strikePrice The strike price (not the strike rate).
   * @param futuresPrice The price of the underlying futures.
   * @return  The volatility and its derivatives with respect to the inputs. In the {@link ValueDerivatives} object,
   * the order of the derivatives are: [0] price, [1] strike, [2] expiry, [3] ATM vol, [4] rho, [5] eta.
   */
  public ValueDerivatives volatilityAdjoint(
      double expiry, double delay, double strikePrice, double futuresPrice) {
    return SSVIVolatilityFunction
        .volatilityAdjoint(futuresPrice, strikePrice, expiry, _volatilityAtm.getYValue(expiry), _rho, _eta);
  }

  @Override
  public MulticurveProviderInterface getMulticurveProvider() {
    return _multicurve;
  }

  @Override
  public double[] parameterSensitivity(String name, List<DoublesPair> pointSensitivity) {
    return _multicurve.parameterSensitivity(name, pointSensitivity);
  }

  @Override
  public double[] parameterForwardSensitivity(String name, List<ForwardSensitivity> pointSensitivity) {
    return _multicurve.parameterForwardSensitivity(name, pointSensitivity);
  }

  @Override
  public Set<String> getAllCurveNames() {
    return _multicurve.getAllCurveNames();
  }

  @Override
  public BlackSTIRFuturesProviderInterface copy() {
    final MulticurveProviderInterface multicurveProvider = _multicurve.copy();
    return new BlackStirFuturesSsviPriceProvider(multicurveProvider, _volatilityAtm, _rho, _eta, _index);
  }

  @Override
  public double getVolatility(double expiry, double delay, double strikePrice, double futuresPrice) {
    return SSVIVolatilityFunction
        .volatility(futuresPrice, strikePrice, expiry, _volatilityAtm.getYValue(expiry), _rho, _eta);
  }

  @Override
  public IborIndex getFuturesIndex() {
    return _index;
  }

}
