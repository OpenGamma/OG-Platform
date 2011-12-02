/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.volatility.BlackFormulaRepository;

/**
 * 
 */
public class EuropeanOptionMarketData {

  private final double _fwd;
  private final double _strike;
  private final double _df;
  private final double _t;
  private final double _vol;
  private final boolean _isCall;
  private double _price;
  private boolean _priceSet;

  public EuropeanOptionMarketData(final double forward, final double strike, final double discountFactor, final double timeToExpiry,
      final boolean isCall, final double impliedVol) {
    Validate.isTrue(forward > 0, "need forward > 0");
    Validate.isTrue(strike >= 0, "need forward >= 0");
    Validate.isTrue(timeToExpiry >= 0, "need timeToExpiry >= 0");
    Validate.isTrue(impliedVol >= 0, "need impliedVol >= 0");

    _fwd = forward;
    _strike = strike;
    _df = discountFactor;
    _t = timeToExpiry;
    _vol = impliedVol;
    _isCall = isCall;
  }

  /**
   * Gets the fwd.
   * @return the fwd
   */
  protected double getForward() {
    return _fwd;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  protected double getStrike() {
    return _strike;
  }

  /**
   * Gets the df.
   * @return the df
   */
  protected double getDiscountFactor() {
    return _df;
  }

  /**
   * Gets the t.
   * @return the t
   */
  protected double getTimeToExpiry() {
    return _t;
  }

  /**
   * Gets the vol.
   * @return the vol
   */
  protected double getImpliedVolatility() {
    return _vol;
  }

  /**
   * Gets the isCall.
   * @return the isCall
   */
  protected boolean isCall() {
    return _isCall;
  }

  public double getPrice() {
    if (!_priceSet) {
      _price = _df * BlackFormulaRepository.price(_fwd, _strike, _t, _vol, _isCall);
      _priceSet = true;
    }
    return _price;
  }

  public EuropeanOptionMarketData withForward(final double forward) {
    return new EuropeanOptionMarketData(forward, _strike, _df, _t, _isCall, _vol);
  }

  public EuropeanOptionMarketData withStrike(final double strike) {
    return new EuropeanOptionMarketData(_fwd, strike, _df, _t, _isCall, _vol);
  }

  public EuropeanOptionMarketData withDiscountFactor(final double discountFactor) {
    return new EuropeanOptionMarketData(_fwd, _strike, discountFactor, _t, _isCall, _vol);
  }

  public EuropeanOptionMarketData withTimeToExpiry(final double timeToExpiry) {
    return new EuropeanOptionMarketData(_fwd, _strike, _df, timeToExpiry, _isCall, _vol);
  }

  public EuropeanOptionMarketData withIsCall(final boolean isCall) {
    return new EuropeanOptionMarketData(_fwd, _strike, _df, _t, isCall, _vol);
  }

  public EuropeanOptionMarketData withImpliedVolatility(final double impliedVolatility) {
    return new EuropeanOptionMarketData(_fwd, _strike, _df, _t, _isCall, impliedVolatility);
  }

  public EuropeanOptionMarketData withPrice(final double price) {
    final double vol = BlackFormulaRepository.impliedVolatility(price / _df, _fwd, _strike, _t, _isCall);
    EuropeanOptionMarketData res = new EuropeanOptionMarketData(_fwd, _strike, _df, _t, _isCall, vol);
    res._price = price;
    res._priceSet = true;
    return res;
  }

}
