/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;
import com.opengamma.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.financial.equity.variance.pricing.VarianceSwapStaticReplication.StrikeParameterization;
import com.opengamma.financial.interestrate.NodeSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityDeltaSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilityFixedStrikeSurface;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;
import com.opengamma.math.surface.InterpolatedSurfaceAdditiveShiftFunction;
import com.opengamma.math.surface.NodalDoublesSurface;
import com.opengamma.util.tuple.DoublesPair;

/**
 * This Calculator provides price sensitivities for the VarianceSwap derivative to changes in 
 * Interest rates,<p>  
 * Equity European Option Volatility,<p> 
 * Spot VarianceSwap contracts,<p>
 * Equity Spot contracts,<p>
 * Equity Forward contracts,<p> 
 * Equity Futures<p> // TODO  CASE !!! Add VarianceFutures
 */
public final class VarianceSwapRatesSensitivityCalculator {
  private static final VarianceSwapRatesSensitivityCalculator INSTANCE = new VarianceSwapRatesSensitivityCalculator();

  public static VarianceSwapRatesSensitivityCalculator getInstance() {
    return INSTANCE;
  }

  private VarianceSwapRatesSensitivityCalculator() {
  }

  /**
   * This calculates the sensitivity of the present value (PV) to a unit move in the forward. <p>
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcForwardSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    // Shift UP
    VarianceSwapDataBundle bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getSpotUnderlying(),
        market.getForwardUnderlying() + shift);
    final double pvUp = pricer.visitVarianceSwap(swap, bumpedMarket);

    // Shift UP
    bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getSpotUnderlying(),
        market.getForwardUnderlying() - shift);
    final double pvDown = pricer.visitVarianceSwap(swap, bumpedMarket);

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to a unit move in the forward, 
   * under a default shift of 1% of the forward <p>
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcForwardSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    final double relativeShift = 0.01;
    final double defaultShift = market.getForwardUnderlying() * relativeShift;
    return calcForwardSensitivity(swap, market, defaultShift);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also know as PVBP and DV01, though note this return per UNIT change in rate. calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a VarianceSwap in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = -(T-t)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)  
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate  
   */
  public Double calcDiscountRateSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(market);
    Validate.notNull(swap);
    // Sensitivity from the discounting
    final StrikeParameterization strikeType = market.getVolatilitySurface().getStrikeParameterisation();
    VarianceSwapStaticReplication pricer = new VarianceSwapStaticReplication(strikeType);
    double pv = pricer.presentValue(swap, market);
    double timeToSettlement = swap.getTimeToSettlement();

    // Sensitivity from forward projection
    double fwdSens = calcForwardSensitivity(swap, market, shift);
    double fwd = market.getForwardUnderlying();

    return timeToSettlement * (fwd * fwdSens - pv);

  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also know as PVBP and DV01, though note this return per UNIT change in rate. calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a VarianceSwap in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = (t-T)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)  
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate  
   */
  public Double calcDiscountRateSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    final double relativeShift = 0.01;
    final double defaultShift = market.getForwardUnderlying() * relativeShift;
    return calcDiscountRateSensitivity(swap, market, defaultShift);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a basis point (bp) move in the funding rates across all maturities. <p>
   * Also know as PVBP and DV01.  
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A Double in the currency, swap.getCurrency()  
   */
  public Double calcPV01(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    return calcDiscountRateSensitivity(swap, market) / 10000;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the continuously-compounded discount rates at the knot points of the funding curve. <p>
   * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in the curve <p>
   * The change of a curve due to the movement of a single knot is interpolator-dependent, so an instrument can have sensitivity to knots at times beyond its maturity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A DoubleMatrix1D containing bucketed delta in order and length of market.getDiscountCurve(). Currency amount per unit amount change in discount rate
   */
  public DoubleMatrix1D calcDeltaBucketed(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    // We know that the VarianceSwap only has true sensitivity to one maturity on one curve. 
    // A function written for interestRate sensitivities spreads this sensitivity across yield nodes 
    // NodeSensitivityCalculator.curveToNodeSensitivities(curveSensitivities, interpolatedCurves)

    // 2nd arg = LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves
    final YieldAndDiscountCurve discCrv = market.getDiscountCurve();
    final String discCrvName = discCrv.getCurve().getName();
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle();
    interpolatedCurves.setCurve(discCrvName, discCrv);

    // 1st arg = Map<String, List<DoublesPair>> curveSensitivities = <curveName, List<(maturity,sensitivity)>> 
    final double settlement = swap.getTimeToSettlement();
    final Double sens = calcDiscountRateSensitivity(swap, market);
    final Map<String, List<DoublesPair>> curveSensitivities = new HashMap<String, List<DoublesPair>>();
    curveSensitivities.put(discCrvName, Lists.newArrayList(new DoublesPair(settlement, sens)));

    NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a NodalDoublesSurface containing the same axes as market.getVolatilitySurface(). <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    final double shift = 0.001; // Shift each vol point by what? +/- 0.1%
    return calcBlackVegaParallel(swap, market, shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a NodalDoublesSurface containing the same axes as market.getVolatilitySurface(). <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    // Unpack market data
    Validate.isTrue(market.getVolatilitySurface().getSurface() instanceof InterpolatedDoublesSurface, "The volatility surface in a VarianceSwapDataBundle must be an InterpolatedDoublesSurface");
    InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) market.getVolatilitySurface().getSurface();
    StrikeParameterization strikeType = market.getVolatilitySurface().getStrikeParameterisation();

    Double[] maturities = blackSurf.getXData();
    Double[] strikes = blackSurf.getYData();
    int nNodes = maturities.length;
    Validate.isTrue(nNodes == strikes.length);

    InterpolatedSurfaceAdditiveShiftFunction volShifter = new InterpolatedSurfaceAdditiveShiftFunction();
    VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    // Parallel shift UP
    InterpolatedDoublesSurface bumpedUp = volShifter.evaluate(blackSurf, shift);
    BlackVolatilitySurface volSurfUp = (strikeType == StrikeParameterization.STRIKE) ?
                                         new BlackVolatilityFixedStrikeSurface(bumpedUp) :
                                           new BlackVolatilityDeltaSurface(bumpedUp, strikeType);
    VarianceSwapDataBundle bumpedMarket = new VarianceSwapDataBundle(volSurfUp, market.getDiscountCurve(), market.getSpotUnderlying(), market.getForwardUnderlying());
    double pvUp = pricer.visitVarianceSwap(swap, bumpedMarket);

    // Parallel shift DOWN
    InterpolatedDoublesSurface bumpedDown = volShifter.evaluate(blackSurf, -shift);
    BlackVolatilitySurface volSurfDown = (strikeType == StrikeParameterization.STRIKE) ?
                                            new BlackVolatilityFixedStrikeSurface(bumpedDown) :
                                              new BlackVolatilityDeltaSurface(bumpedDown, strikeType);
    bumpedMarket = new VarianceSwapDataBundle(volSurfDown, market.getDiscountCurve(), market.getSpotUnderlying(), market.getForwardUnderlying());
    double pvDown = pricer.visitVarianceSwap(swap, bumpedMarket);

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currencys amount per unit amount change in the black volatility of each node
  */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    final double shift = 0.001; // Shift each vol point by what? +/- 0.1%
    return calcBlackVegaForEntireSurface(swap, market, shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currencys amount per unit amount change in the black volatility of each node
  */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    // Unpack market data
    Validate.isTrue(market.getVolatilitySurface().getSurface() instanceof InterpolatedDoublesSurface,
        "Currently will only accept a Equity VolatilitySurfaces based on an InterpolatedDoublesSurface");

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) market.getVolatilitySurface().getSurface();
    final Double[] maturities = blackSurf.getXData();
    final Double[] strikes = blackSurf.getYData();
    final int nNodes = maturities.length;
    Validate.isTrue(nNodes == strikes.length);

    // Bump and reprice
    final Double[] vegas = new Double[nNodes];
    for (int j = 0; j < nNodes; j++) {
      vegas[j] = calcBlackVegaForSinglePoint(swap, market, maturities[j], strikes[j], shift);
    }

    return new NodalDoublesSurface(maturities, strikes, vegas);
  }

  /**
   * Compute the price sensitivity to a shift of the Black volatility at a given maturity and strike. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity.<p>
   * Important!!! If the <i>(x, y)</i> value(s) of the shift(s) are not in the nodal points of the original surface, they are added (with shift) to the nodal points of the new surface. 
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param maturity a double in same unit as VolatilitySurface
   * @param strike a double in same unit as VolatilitySurface
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return Currency amount per unit amount change in the black volatility at the point provided
   */
  public double calcBlackVegaForSinglePoint(final VarianceSwap swap, final VarianceSwapDataBundle market, double maturity, double strike, final double shift) {

    final VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) market.getVolatilitySurface().getSurface();
    final StrikeParameterization strikeType = market.getVolatilitySurface().getStrikeParameterisation();
    final InterpolatedSurfaceAdditiveShiftFunction volShifter = new InterpolatedSurfaceAdditiveShiftFunction();

    // shift UP
    final InterpolatedDoublesSurface bumpedVolUp = volShifter.evaluate(blackSurf, maturity, strike, shift);
    final BlackVolatilitySurface volSurfUp = (strikeType == StrikeParameterization.STRIKE) ?
                                                new BlackVolatilityFixedStrikeSurface(bumpedVolUp) :
                                                  new BlackVolatilityDeltaSurface(bumpedVolUp, strikeType);
    final VarianceSwapDataBundle bumpedMarket = new VarianceSwapDataBundle(volSurfUp, market.getDiscountCurve(), market.getSpotUnderlying(), market.getForwardUnderlying());
    final double pvUp = pricer.visitVarianceSwap(swap, bumpedMarket);

    // shift DOWN
    final InterpolatedDoublesSurface bumpedVolDown = volShifter.evaluate(blackSurf, maturity, strike, -shift);
    final BlackVolatilitySurface volSurfDown = (strikeType == StrikeParameterization.STRIKE) ?
                                                  new BlackVolatilityFixedStrikeSurface(bumpedVolDown) :
                                                    new BlackVolatilityDeltaSurface(bumpedVolDown, strikeType);
    final VarianceSwapDataBundle bumpedMarketUp = new VarianceSwapDataBundle(volSurfDown, market.getDiscountCurve(), market.getSpotUnderlying(), market.getForwardUnderlying());
    final double pvDown = pricer.visitVarianceSwap(swap, bumpedMarketUp);

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  /**
   * This calculates the derivative of the present value (PV) with respect to the level of the fair value of variance 
   * of a spot starting swap with an expiry equal to the that remaining in the existing VarianceSwap, 
   * as described by David E Kuenzi in Risk 2005, 'Variance swaps and non-constant vega' <p>
   * This is simply the proportion of time left in the existing swap.
   * <p>
   *     
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @return A Double representing the number of spot-starting VarianceSwaps required to hedge the variance exposure
   */
  public Double calcSensitivityToFairVariance(final VarianceSwap swap, final VarianceSwapDataBundle market) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    int nObsExpected = swap.getObsExpected();
    int nObsSoFar = swap.getObservations().length;
    int nObsDidntHappen = swap.getObsDisrupted();

    return (nObsExpected - nObsSoFar - nObsDidntHappen) / nObsExpected * swap.getVarNotional();
  }

}
