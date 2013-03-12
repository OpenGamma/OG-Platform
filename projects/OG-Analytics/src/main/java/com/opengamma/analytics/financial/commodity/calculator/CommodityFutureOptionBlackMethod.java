/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculator;

import com.opengamma.analytics.financial.commodity.derivative.CommodityFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Black methods for commodity future option prices and greeks.
 */
public final class CommodityFutureOptionBlackMethod {
  /** A static instance of this class */
  private static final CommodityFutureOptionBlackMethod INSTANCE = new CommodityFutureOptionBlackMethod();

  /**
   * @return The static instance of this class
   */
  public static CommodityFutureOptionBlackMethod getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor
   */
  private CommodityFutureOptionBlackMethod() {
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the <b>forward</b> price of an option using the Black formula. PV / ZeroBond(timeToSettlement)
   */
  public double forwardPrice(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double notional = derivative.getUnderlying().getUnitAmount();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdPrice = BlackFormulaRepository.price(forward, strike, expiry, blackVol, derivative.isCall());
    return notional * fwdPrice;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return Current DiscountBond or ZeroBond price for payment at the settlement date
   */
  public double discountToSettlement(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double df = marketData.getDiscountCurve().getDiscountFactor(derivative.getUnderlying().getSettlement());
    return df;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity
   */
  public double forwardIndexValue(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    return forward;
  }

  /**
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the <b>spot</b> value of the index, i.e. the current market value
   */
  public double spotIndexValue(final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(marketData, "marketData");
    final double spot = marketData.getForwardCurve().getSpot();
    return spot;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the present value of the option
   */
  public double presentValue(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double fwdPrice = forwardPrice(derivative, marketData);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdPrice;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the sensitivity of the present value wrt the discounting rate
   */
  public double rho(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double ttm = derivative.getUnderlying().getSettlement();
    final double pv = presentValue(derivative, marketData);
    return -ttm * pv;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the delta
   */
  public double delta(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final boolean isCall = derivative.isCall();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    return BlackFormulaRepository.delta(forward, strike, expiry, blackVol, isCall);
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the forward delta wrt the forward underlying, ie the sensitivity of the undiscounted price to the forward value of the underlying, d(PV/Z)/d(fwdUnderlying)
   */
  public double forwardDelta(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double undiscountDelta = delta(derivative, marketData);
    return undiscountDelta;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the sensitivity of the present value wrt the forward value of the underlying, d(PV)/d(fwdUnderlying)
   */
  public double deltaWrtForward(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double forwardDelta = forwardDelta(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardDelta * zeroBond;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot delta wrt the underlying, d(PV)/d(spotUnderlying)
   */
  public double deltaWrtSpot(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double deltaWrtForward = deltaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return deltaWrtForward * dForwardDSpot;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the forward gamma wrt the forward underlying, ie the 2nd order sensitivity of the undiscounted price to the forward value of the underlying,
   *          $\frac{\partial^2 (PV/Z)}{\partial F^2}$
   */
  public double forwardGamma(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardGamma = BlackFormulaRepository.gamma(forward, strike, expiry, blackVol);
    return forwardGamma;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the sensitivity of the forward delta wrt the forward value of the underlying, $\frac{\partial^2 (PV)}{\partial F^2}$
   */
  public double gammaWrtForward(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double forwardGamma = forwardGamma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardGamma * zeroBond;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot gamma wrt the spot underlying, ie the 2nd order sensitivity of the present value to the spot value of the underlying,
   *          $\frac{\partial^2 (PV)}{\partial S^2}$
   */
  public double gammaWrtSpot(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double gammaWrtForward = gammaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return gammaWrtForward * dForwardDSpot * dForwardDSpot;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the simple vega, d(PV)/d(blackVol)
   */
  public double vega(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double fwdVega = BlackFormulaRepository.vega(forward, strike, expiry, blackVol);
    final double df = discountToSettlement(derivative, marketData);
    return df * fwdVega;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the lognormal Black Volatility
   */
  public double impliedVol(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    return blackVol;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the forward Vomma, ie the 2nd order sensitivity of the undiscounted price to the implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial \sigma^2}$
   */
  public double forwardVomma(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardVomma = BlackFormulaRepository.vomma(forward, strike, expiry, blackVol);
    return forwardVomma;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot Vomma, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double vomma(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double forwardVomma = forwardVomma(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVomma * zeroBond;
  }

  /**
   * Synonym for Vomma
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot Volga, ie the 2nd order sensitivity of the spot price to the implied vol,
   *          $\frac{\partial^2 (PV)}{\partial \sigma^2}$
   */
  public double spotVolga(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    return vomma(derivative, marketData);
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the forward vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the undiscounted price to the forward and implied vol,
   *          $\frac{\partial^2 (PV/Z)}{\partial F \partial \sigma}$
   */
  public double forwardVanna(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double strike = derivative.getStrike();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardVanna = BlackFormulaRepository.vanna(forward, strike, expiry, blackVol);
    return forwardVanna;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot vanna wrt the forward underlying, ie the 2nd order cross-sensitivity of the present value to the forward and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial F \partial \sigma}$
   */
  public double vannaWrtForward(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double forwardVanna = forwardVanna(derivative, marketData);
    final double zeroBond = discountToSettlement(derivative, marketData);
    return forwardVanna * zeroBond;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the spot vanna wrt the spot underlying, ie the 2nd order cross-sensitivity of the present value to the spot and implied vol,
   *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
   */
  public double vannaWrtSpot(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double vannaWrtForward = vannaWrtForward(derivative, marketData);
    final double forwardUnderlying = forwardIndexValue(derivative, marketData);
    final double spotUnderlying = marketData.getForwardCurve().getSpot();
    final double dForwardDSpot = forwardUnderlying / spotUnderlying;
    return vannaWrtForward * dForwardDSpot;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return Spot theta, ie the sensitivity of the present value to the time to expiration,
   *          $\frac{\partial (PV)}{\partial t}$
   */
  public double theta(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double interestRate = marketData.getDiscountCurve().getInterestRate(expiry);
    final double theta = BlackFormulaRepository.theta(forward, strike, expiry, blackVol, derivative.isCall(), interestRate);
    return theta;
  }

  /**
   * @param derivative the OG-Analytics form of the derivative
   * @param marketData the data bundle containing a BlackVolatilitySurface, forward commodity and funding curves
   * @return the forward (i.e. driftless) theta
   */
  public double forwardTheta(final CommodityFutureOption<?> derivative, final StaticReplicationDataBundle marketData) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(marketData, "marketData");
    final double expiry = derivative.getExpiry();
    final double forward = marketData.getForwardCurve().getForward(expiry);
    final double strike = derivative.getStrike();
    final double blackVol = marketData.getVolatilitySurface().getVolatility(expiry, strike);
    final double forwardTheta = BlackFormulaRepository.driftlessTheta(forward, strike, expiry, blackVol);
    return forwardTheta;
  }
}
