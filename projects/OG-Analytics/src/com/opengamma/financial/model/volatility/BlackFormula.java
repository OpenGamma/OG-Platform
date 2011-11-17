/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.NewtonRaphsonSingleRootFinder;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * Black pricing in the forward measure. All prices, input/output, are *forward* prices, i.e. price(t,T) / Zero(t,T).
 * Similar to http://en.wikipedia.org/wiki/Black_model with fwdMtm = c / exp(-rT). 
 * This permits us to disregard discounting, which is sufficient for purpose of implied volatility.   
 */
public class BlackFormula {
  private double _forward;
  private double _strike;
  private double _expiry;
  private Double _lognormalVol;
  private Double _fwdMtm;
  private boolean _isCall;

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
  public BlackFormula(final double forward, final double strike, final double expiry, final Double lognormalVol, final Double fwdPrice, final boolean isCall) {
    _forward = forward;
    _strike = strike;
    _expiry = expiry;
    _lognormalVol = lognormalVol;
    _fwdMtm = fwdPrice;
    _isCall = isCall;
  }

  /**
   * Computes the forward price from forward, strike and variance. 
   * This is NOT a getter of the data member, _fwdMtm. For that, use getFwdMtm().
   * Nor does this set any data member.
   * @return Black formula price of the option
   */
  public final double computePrice() {
    Validate.notNull(_lognormalVol, "Black Volatility parameter, _vol, has not been set.");
    return BlackFormulaRepository.price(_forward, _strike, _expiry, _lognormalVol, _isCall);
  }

  /**
   * Computes the derivative of the *forward price* with respect to the forward.
   * If the current value of a call with expiry, T is C(0,T), the forward price is this normalized by the terminal bond, Z(0,T) = exp(-rT).
   * Hence *forward price* c(0,T) = C(0,T) / Z(0,T). And ForwardDelta = dc/dF == d(C/Z)/dF   
   * This is NOT a getter of the data member, _fwdMtm. For that, use getFwdMtm().
   * Nor does this set any data member.
   * @return  d/dF [C(0,T) / Z(0,T)] 
   */
  public final double computeForwardDelta() {
    Validate.notNull(_lognormalVol, "Black Volatility parameter, _vol, has not been set.");

    if (_strike < SMALL) {
      return _isCall ? 1.0 : 0.0;
    }
    final int sign = _isCall ? 1 : -1;
    final double sigmaRootT = _lognormalVol * Math.sqrt(_expiry);
    if (Math.abs(_forward - _strike) < SMALL) {
      return sign * NORMAL.getCDF(sign * 0.5 * sigmaRootT);
    }
    if (sigmaRootT < SMALL) {
      return sign;
    }
    final double d1 = Math.log(_forward / _strike) / sigmaRootT + 0.5 * sigmaRootT;
    final double delta = sign * NORMAL.getCDF(sign * d1);
    return delta;
  }

  /**
   * Via the BlackFormula, this converts a Delta-parameterised strike into the actual strike value.
   * fwdDelta, defined here, is always positive, in [0,1]. At 0.5, a straddle has zero delta, occurring at K == F*exp(0.5 sig^2 T). So deltas < 0.5 are out-of-the-money.
   * Hence a 0.25 put and a 0.25 call will have a strike less and greater than the forward, respectively. 
   * @param fwdDelta the first order derivative of the price with respect to the forward
   * @param forCall Choose whether you would like to see the strike of the call or put for the fwdDelta provided
   * @return The true strike value
   */
  public final Double computeStrikeImpliedByForwardDelta(final double fwdDelta, final boolean forCall) {
    Validate.isTrue(fwdDelta >= 0.0 && fwdDelta <= 1.0, "Delta must be between 0.0 and 1.0");
    final double sign = forCall ? 1. : -1.;
    final double d1 = sign * NORMAL.getInverseCDF(fwdDelta);
    final double sigmaRootT = _lognormalVol * Math.sqrt(_expiry);
    final double strike = _forward * Math.exp(sigmaRootT * (0.5 * sigmaRootT - d1));
    return strike;
  }

  public final double computeStrikeSensitivity() {
    Validate.notNull(_lognormalVol, "Black Volatility parameter, _vol, has not been set.");

    if (_strike < SMALL) {
      return _isCall ? -1.0 : 0.0;
    }
    final int sign = _isCall ? 1 : -1;
    final double sigmaRootT = _lognormalVol * Math.sqrt(_expiry);
    if (Math.abs(_forward - _strike) < SMALL) {
      return -sign * NORMAL.getCDF(sign * 0.5 * sigmaRootT);
    }
    if (sigmaRootT < SMALL) {
      return -sign;
    }
    final double d2 = Math.log(_forward / _strike) / sigmaRootT - 0.5 * sigmaRootT;
    return -sign * NORMAL.getCDF(sign * d2);
  }

  /**
   * Computes the implied lognormal volatility from forward mark-to-market, forward underlying and strike. 
   * This is NOT a getter of the data member, _lognormalVol. For that, use getLognormalVol().
   * Nor does this set any data member.
   * @return Black formula price of the option
   */
  public final double computeImpliedVolatility() {
    Validate.notNull(_fwdMtm, "price is not set. Cannot compute implied volatility. call setMtm first");
    return BlackFormulaRepository.impliedVolatility(_fwdMtm.doubleValue(), _forward, _strike, _expiry, _isCall);
    //    try {
    //      final BlackFunctionData blackData = new BlackFunctionData(_forward, 1.0, 0.0);
    //      final EuropeanVanillaOption blackOption = new EuropeanVanillaOption(_strike, _expiry, _isCall);
    //      final double impVol = new BlackImpliedVolatilityFormula().getImpliedVolatility(blackData, blackOption, _fwdMtm);
    //      return impVol;
    //    } catch (final com.opengamma.math.MathException e) {
    //      System.err.println("Failed to compute ImpliedVolatility");
    //      throw new OpenGammaRuntimeException(e.getMessage());
    //    }
  }

  // The following function was used to test the other. 
  // Will likely be useful when dealing with manipulating strike and vol as they depend on each other in delta parameterisation
  public final Double computeStrikeImpliedByDeltaViaRootFinding(final double fwdDelta, final boolean forCall) {
    try {
      Validate.isTrue(fwdDelta >= 0.0 && fwdDelta <= 1.0, "Delta must be between 0.0 and 1.0");
      final BlackFormula black = new BlackFormula(_forward, _strike, _expiry, _lognormalVol, _fwdMtm, forCall);

      final Function1D<Double, Double> difference = new Function1D<Double, Double>() {

        @Override
        public Double evaluate(final Double strike) {
          black.setStrike(strike);
          final double delta = black.computeForwardDelta();
          return delta - fwdDelta * (forCall ? 1.0 : -1.0); // TODO Case : confirm this is sufficient for calls and puts
        }
      };

      final NewtonRaphsonSingleRootFinder rootFinder = new NewtonRaphsonSingleRootFinder();
      if ((forCall && fwdDelta >= 0.5) || (!forCall && fwdDelta <= 0.5)) {
        // Strike is bounded below by 0 and above by the atmDelta
        final double atmDelta = _forward * Math.exp(0.5 * _lognormalVol * _lognormalVol * _expiry);
        return rootFinder.getRoot(difference, 0.0, atmDelta);
      } // Give it a guess, and estimate finite-difference derivative
      return rootFinder.getRoot(difference, _strike);

    } catch (final com.opengamma.math.MathException e) {
      System.err.println(e);
      System.err.println("Failed to compute ImpliedVolatility");
      return null;
    }
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
  public final void setForward(final double forward) {
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
  public final void setStrike(final double strike) {
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
  public final void setExpiry(final double expiry) {
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
  public final void setLognormalVol(final Double vol) {
    Validate.isTrue(vol > 0.0 || CompareUtils.closeEquals(vol, 0.0), "Cannot set vol to be negative.");
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
  public final void setMtm(final Double fwdMtm) {
    _fwdMtm = fwdMtm;
  }

  /**
   * Sets the forward mark-to-market price.
   * @param bCall  True if a call, false if a put
   */
  public final void setIsCall(final boolean bCall) {
    _isCall = bCall;
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BlackFormula)) {
      return false;
    }
    final BlackFormula other = (BlackFormula) obj;
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
