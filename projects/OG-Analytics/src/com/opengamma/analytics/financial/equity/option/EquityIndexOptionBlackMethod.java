/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;

import org.apache.commons.lang.Validate;

/**
 * Pricing method for vanilla Equity Index Option transactions with Black function.
 */
public final class EquityIndexOptionBlackMethod {

  /** TODO What else?
   * Delta wrt Fwd
   * Delta wrt Strike (DualDelta)
   * Gamma (spot, fwd, strike)
   * Vega (wrt impliedVol surface)
   * Rates Delta (again, single rate, and curve)
   */

  private static final EquityIndexOptionBlackMethod INSTANCE = new EquityIndexOptionBlackMethod();

  public static EquityIndexOptionBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private EquityIndexOptionBlackMethod() {
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> price of an option using the Black formula. PV / ZeroBond(timeToSettlement) 
   */
  public double forwardPrice(EquityIndexOption derivative, EquityOptionDataBundle marketData) {
    Validate.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdPrice = BlackFormulaRepository.price(forward, strike, expiry, blackVol, derivative.isCall());
    return fwdPrice;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return Current DiscountBond or ZeroBond price for payment at the settlement date 
   */
  public double discountToSettlement(EquityIndexOption derivative, EquityOptionDataBundle marketData) {
    final double df = marketData.getDiscountCurve().getDiscountFactor(derivative.getTimeToSettlement());
    return df;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity 
   */
  public double forwardIndexValue(EquityIndexOption derivative, EquityOptionDataBundle marketData) {
    Validate.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    return forward;
  }

  /** 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>spot</b> value of the index, ie the current market value 
   */
  public double spotIndexValue(EquityOptionDataBundle marketData) {
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double spot = marketData.getForwardCurve().getSpot();
    return spot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The present value of the option 
   */
  public double presentValue(EquityIndexOption derivative, EquityOptionDataBundle marketData) {
    final double fwdPrice = forwardPrice(derivative, marketData);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdPrice;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An YieldCurveBundle, which won't work
   * @return OpenGammaRuntimeException
   */
  public double presentValue(final EquityIndexOption derivative, final YieldCurveBundle marketData) {
    Validate.notNull(derivative, "The derivative, EquityIndexOption, was null.");
    Validate.notNull(marketData, "DataBundle was null. Expecting an EquityOptionDataBundle");
    throw new OpenGammaRuntimeException("EquityIndexOptionBlackMethod requires a data bundle of type EquityOptionDataBundle. Found a YieldCurveBundle.");
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward delta wrt the forward underlying, ie the sensitivity of the undiscounted price to the forward value of the underlying, d(PV/Z)/d(fwdUnderlying)
   */
  public double forwardDelta(final EquityIndexOption derivative, final EquityOptionDataBundle marketData) {
    Validate.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final boolean isCall = derivative.isCall();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double undiscountDelta = BlackFormulaRepository.delta(forward, strike, expiry, blackVol, isCall);
    return undiscountDelta;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the forward value of the underlying, d(PV)/d(fwdUnderlying) 
   */
  public double deltaWrtForward(final EquityIndexOption derivative, final EquityOptionDataBundle marketData) {
    final double forwardDelta = forwardDelta(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardDelta * zeroBond;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot delta wrt the underlying, d(PV)/d(spotUnderlying) 
   */
  public double deltaWrtSpot(final EquityIndexOption derivative, final EquityOptionDataBundle marketData) {
    final double deltaWrtForward = deltaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return deltaWrtForward * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The simple vega, d(PV)/d(blackVol) 
   */
  public double vega(final EquityIndexOption derivative, final EquityOptionDataBundle marketData) {
    Validate.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdVega = BlackFormulaRepository.vega(forward, strike, expiry, blackVol);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdVega;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData An EquityOptionDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The lognormal Black Volatility 
   */
  public double impliedVol(final EquityIndexOption derivative, final EquityOptionDataBundle marketData) {
    Validate.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    Validate.notNull(marketData, "market was null. Expecting EquityOptionDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    return blackVol;
  }

}
