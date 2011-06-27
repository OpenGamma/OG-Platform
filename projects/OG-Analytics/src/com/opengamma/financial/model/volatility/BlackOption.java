/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * Black pricing in the forward measure. All prices, input/output, are *forward* prices, i.e. price(t,T) / Zero(t,T).
 * Similar to http://en.wikipedia.org/wiki/Black_model with fwdMtm = c / exp(-rT). 
 * This permits us to disregard discounting, which is sufficient for purpose of implied volatility.   
 */
public class BlackOption {
  private double _forward;
  private double _strike;
  private double _expiry;
  private Double _lognormalVol;
  private Double _fwdMtm;
  private final boolean _isCall; // TODO Better as an enum: optionType = { CALL, PUT, STRADDLE } because the latter is frequently used

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  private static final double SMALL = 1.0E-12;

  /**
   * @param forward Forward value of the underlying. The fair value agreed today to exchange for the underlying at expiry
   * @param strike Strike of the option in question. If equal to the forward, it is considered at-the-money 
   * @param expiry Expiry date, as a double representing the number of years until the option expires 
   * @param lognormalVol Average lognormal volatility of the underlying that reproduces the price. May be null.  
   * @param fwdPrice Forward, i.e. undiscounted, price of the option. May be null.
   * @param isCall True if the option is a Call; false if it is a Put. 
   */
  public BlackOption(double forward, double strike, double expiry, Double lognormalVol, Double fwdPrice, boolean isCall) {
    _forward = forward;
    _strike = strike;
    _expiry = expiry;
    _lognormalVol = lognormalVol;
    _fwdMtm = fwdPrice;
    _isCall = isCall;
  }

  /**
   * Computes the forward price from forward, strike and variance. 
   * This does NOT a getter of the data member, _fwdMtm. For that, use getFwdMtm().
   * @return Black formula price of the option
   */
  public final double getPrice() {
    Validate.notNull(_lognormalVol, "Black Volatility parameter, _vol, has not been set.");

    if (_strike < SMALL) {
      return _isCall ? _forward : 0.0;
    }
    final int sign = _isCall ? 1 : -1;
    final double sigmaRootT = _lognormalVol * Math.sqrt(_expiry);
    if (Math.abs(_forward - _strike) < SMALL) {
      return _forward * (2 * NORMAL.getCDF(sigmaRootT / 2) - 1);
    }
    if (sigmaRootT < SMALL) {
      return Math.max(sign * (_forward - _strike), 0.0);
    }

    final double d1 = Math.log(_forward / _strike) / sigmaRootT + 0.5 * sigmaRootT;
    final double d2 = d1 - sigmaRootT;

    return sign * (_forward * NORMAL.getCDF(sign * d1) - _strike * NORMAL.getCDF(sign * d2));
  }

  public final double getStrikeSensitivity() {
    Validate.notNull(_lognormalVol, "Black Volatility parameter, _vol, has not been set.");

    if (_strike < SMALL) {
      return _isCall ? _forward : 0.0;
    }
    final int sign = _isCall ? 1 : -1;
    final double sigmaRootT = _lognormalVol * Math.sqrt(_expiry);
    if (Math.abs(_forward - _strike) < SMALL) {
      return -sign * NORMAL.getCDF(sign * 0.5 * sigmaRootT);
    }
    if (sigmaRootT < SMALL) {
      return sign;
    }
    final double d2 = Math.log(_forward / _strike) / sigmaRootT - 0.5 * sigmaRootT;
    return -sign * NORMAL.getCDF(sign * d2);
  }

  /**
   * Gets the forward.
   * @return the forward
   */
  public final double getForward() {
    return _forward;
  }

  /**
   * Sets the forward.
   * @param forward  the forward
   */
  public final void setForward(double forward) {
    _forward = forward;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public final double getStrike() {
    return _strike;
  }

  /**
   * Sets the strike.
   * @param strike  the strike
   */
  public final void setStrike(double strike) {
    _strike = strike;
  }

  /**
   * Gets the expiry.
   * @return the expiry
   */
  public final double getExpiry() {
    return _expiry;
  }

  /**
   * Sets the expiry.
   * @param expiry  the expiry
   */
  public final void setExpiry(double expiry) {
    _expiry = expiry;
  }

  /**
   * Gets the vol.
   * @return the vol
   */
  public final Double getLognormalVol() {
    return _lognormalVol;
  }

  /**
   * Sets the vol.
   * @param vol  the vol
   */
  public final void setLognormalVol(Double vol) {
    _lognormalVol = vol;
  }

  /**
   * Returns the member, _FwdMtm, the forward mark-to-market price. It DOES NOT compute from vol. For that, use getPrice.
   * @return the fwdMtm
   */
  public final Double getFwdMtm() {
    return _fwdMtm;
  }

  /**
   * Sets the forward mark-to-market price.
   * @param fwdMtm  forward mark-to-market price
   */
  public final void setMtm(Double fwdMtm) {
    _fwdMtm = fwdMtm;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_forward);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_fwdMtm == null) ? 0 : _fwdMtm.hashCode());
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + ((_lognormalVol == null) ? 0 : _lognormalVol.hashCode());
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackOption)) {
      return false;
    }
    BlackOption other = (BlackOption) obj;
    if (Double.doubleToLongBits(_expiry) != Double.doubleToLongBits(other._expiry)) {
      return false;
    }
    if (Double.doubleToLongBits(_forward) != Double.doubleToLongBits(other._forward)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (!ObjectUtils.equals(_fwdMtm, other._fwdMtm)) {
      return false;
    }
    if (!ObjectUtils.equals(_lognormalVol, other._lognormalVol)) {
      return false;
    }
    return true;
  }

}
