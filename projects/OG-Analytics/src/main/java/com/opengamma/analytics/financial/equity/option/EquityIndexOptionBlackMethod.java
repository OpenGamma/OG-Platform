/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Pricing method for vanilla Equity Index Option transactions with Black function.
 */
//TODO there is a lot of repeated code in this class and EquityOptionBlackMethod
public final class EquityIndexOptionBlackMethod {

  private static final EquityIndexOptionBlackMethod INSTANCE = new EquityIndexOptionBlackMethod();

  /**
   * @return The static instance
   */
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
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> price of an option using the Black formula. PV / ZeroBond(timeToSettlement) 
   */
  public double forwardPrice(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double notional = derivative.getUnitAmount();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdPrice = BlackFormulaRepository.price(forward, strike, expiry, blackVol, derivative.isCall());
    return notional * fwdPrice;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return Current DiscountBond or ZeroBond price for payment at the settlement date 
   */
  public double discountToSettlement(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double df = marketData.getDiscountCurve().getDiscountFactor(derivative.getTimeToSettlement());
    return df;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity 
   */
  public double forwardIndexValue(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    return forward;
  }

  /** 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The <b>spot</b> value of the index, i.e. the current market value 
   */
  public double spotIndexValue(final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double spot = marketData.getForwardCurve().getSpot();
    return spot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The present value of the option 
   */
  public double presentValue(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double fwdPrice = forwardPrice(derivative, marketData);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdPrice;
  }

  /** 
   * Computes the sensitivity of the present value wrt the discounting rate assuming one is hedging with the Forward, F. <p>
   * In this case, the arguments d1,d2 in the cumulative normal calls have no rates dependence. 
   * The only sensitivity comes from the discounting itself, hence dV/dr = (T-t) * V. <p> 
   * This differs from rhoBlackScholes in which one forms a hedging portfolio with a discount bond and the SPOT, S.
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the discounting rate 
   */
  public double rhoBlack(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double ttm = derivative.getTimeToSettlement();
    final double pv = presentValue(derivative, marketData) / derivative.getUnitAmount();
    return -ttm * pv / 100.;
  }

  /** 
   * Computes the sensitivity of the present value wrt the discounting rate assuming one is hedging with the Spot underlying, S. <p>
   * This differs from rhoBlack in which one forms a hedging portfolio with a discount bond and the Forward, F.
   * @param derivative An EquityOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the discounting rate 
   */
  public double rhoBlackScholes(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double ttm = derivative.getTimeToExpiry();
    final double forward = marketData.getForwardCurve().getForward(ttm);
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(ttm, strike);
    final boolean isCall = derivative.isCall();
    final double dualDelta = BlackFormulaRepository.dualDelta(forward, strike, ttm, blackVol, isCall);
    final double df = discountToSettlement(derivative, marketData);
    return strike * ttm * df * dualDelta * (isCall ? -1.0 : 1.0);
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward delta wrt the forward underlying, ie the sensitivity of the undiscounted price to the forward value of the underlying, d(PV/Z)/d(fwdUnderlying)
   */
  public double forwardDelta(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
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
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the present value wrt the forward value of the underlying, d(PV)/d(fwdUnderlying) 
   */
  public double deltaWrtForward(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardDelta = forwardDelta(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardDelta * zeroBond;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot delta wrt the underlying, d(PV)/d(spotUnderlying) 
   */
  public double deltaWrtSpot(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double deltaWrtForward = deltaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return deltaWrtForward * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward gamma wrt the forward underlying, ie the 2nd order sensitivity of the undiscounted price to the forward value of the underlying,
   *          $\frac{\partial^2 (PV/Z)}{\partial F^2}$
   */
  public double forwardGamma(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardGamma = BlackFormulaRepository.gamma(forward, strike, expiry, blackVol);
    return forwardGamma;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The sensitivity of the forward delta wrt the forward value of the underlying, $\frac{\partial^2 (PV)}{\partial F^2}$ 
   */
  public double gammaWrtForward(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardGamma = forwardGamma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardGamma * zeroBond;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot gamma wrt the spot underlying, ie the 2nd order sensitivity of the present value to the spot value of the underlying,
   *          $\frac{\partial^2 (PV)}{\partial S^2}$
   */
  public double gammaWrtSpot(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double gammaWrtForward = gammaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return gammaWrtForward * dForwardDSpot * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The simple vega, d(PV)/d(blackVol) 
   */
  public double vega(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
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
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The lognormal Black Volatility 
   */
  public double impliedVol(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    return blackVol;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward Vomma, ie the 2nd order sensitivity of the undiscounted price to the implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial \sigma^2}$
   */
  public double forwardVomma(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardVomma = BlackFormulaRepository.vomma(forward, strike, expiry, blackVol);
    return forwardVomma;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot Vomma, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double vomma(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardVomma = forwardVomma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVomma * zeroBond;
  }

  /** 
   * Synonym for Vomma
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot Volga, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double spotVolga(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    return vomma(derivative, marketData);
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The forward vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the undiscounted price to the forward and implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial F \partial \sigma}$
   */
  public double forwardVanna(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardVanna = BlackFormulaRepository.vanna(forward, strike, expiry, blackVol);
    return forwardVanna;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the present value to the forward and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial F \partial \sigma}$
   */
  public double vannaWrtForward(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double forwardVanna = forwardVanna(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVanna * zeroBond;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return The spot vanna wrt the spot underlying, ie the 2nd order cross-sensitivity of the present value to the spot and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
   */
  public double vannaWrtSpot(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    final double vannaWrtForward = vannaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return vannaWrtForward * dForwardDSpot;
  }

  /** 
   * @param derivative An EquityIndexOption, the OG-Analytics form of the derivative 
   * @param marketData A StaticReplicationDataBundle, containing a BlackVolatilitySurface, forward equity and funding curves
   * @return Spot theta, ie the sensitivity of the present value to the time to expiration,
   *          $\frac{\partial (PV)}{\partial \tau}$
   */
  public double spotTheta(final EquityIndexOption derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative was null. Expecting EquityIndexOption");
    ArgumentChecker.notNull(marketData, "market was null. Expecting StaticReplicationDataBundle");
    final double expiry = derivative.getTimeToExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double theta = BlackFormulaRepository.driftlessTheta(forward, strike, expiry, blackVol);
    return -1 * theta; // *-1 as BlackFormulaRepository gives dV/dt, and we want dV/d(T-t)
  }

}
