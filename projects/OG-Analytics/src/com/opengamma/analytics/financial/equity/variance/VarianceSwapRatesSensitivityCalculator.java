/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.equity.variance.derivative.VarianceSwap;
import com.opengamma.analytics.financial.equity.variance.pricing.VarianceSwapStaticReplication;
import com.opengamma.analytics.financial.interestrate.NodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedSurfaceAdditiveShiftFunction;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
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
   * This calculates the sensitivity of the present value (PV) to a unit move in the forward.
   * The volatility surface remains unchanged.
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param relShift Relative size of shift made in centered-finite difference approximation.
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  @SuppressWarnings({ })
  public Double calcForwardSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market, final double relShift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    final VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    // Shift UP
    VarianceSwapDataBundle bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve().withFractionalShift(relShift));
    final double pvUp = pricer.visitVarianceSwap(swap, bumpedMarket);

    // Shift Down
    bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve().withFractionalShift(-relShift));
    final double pvDown = pricer.visitVarianceSwap(swap, bumpedMarket);

    final double t = swap.getTimeToSettlement();
    final double fwd = market.getForwardCurve().getForward(t);
    // Centered-difference result
    return (pvUp - pvDown) / 2.0 / relShift / fwd;
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
    return calcForwardSensitivity(swap, market, relativeShift);
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
   * @param shift Relative size of shift made in centered-finite difference approximation.
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate
   */
  @SuppressWarnings({ })
  public Double calcDiscountRateSensitivity(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(market);
    Validate.notNull(swap);
    // Sensitivity from the discounting

    final VarianceSwapStaticReplication pricer = new VarianceSwapStaticReplication();
    final double pv = pricer.presentValue(swap, market);
    final double timeToSettlement = swap.getTimeToSettlement();

    // Sensitivity from forward projection
    final double fwdSens = calcForwardSensitivity(swap, market, shift);
    final double fwd = market.getForwardCurve().getForward(timeToSettlement);

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
    return calcDiscountRateSensitivity(swap, market, relativeShift);
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

    final NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
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
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param swap the VarianceSwap
   * @param market the VarianceSwapDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  @SuppressWarnings({ })
  public Double calcBlackVegaParallel(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    final VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    // Parallel shift UP
    final BlackVolatilitySurface<?> upSurface = market.getVolatilitySurface().withShift(shift, true);
    final double pvUp = pricer.visitVarianceSwap(swap, new VarianceSwapDataBundle(upSurface, market.getDiscountCurve(), market.getForwardCurve()));

    // Parallel shift DOWN
    final BlackVolatilitySurface<?> downSurface = market.getVolatilitySurface().withShift(-shift, true);
    final double pvDown = pricer.visitVarianceSwap(swap, new VarianceSwapDataBundle(downSurface, market.getDiscountCurve(), market.getForwardCurve()));

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
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currency amount per unit amount change in the black volatility of each node
   */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final VarianceSwap swap, final VarianceSwapDataBundle market, final double shift) {
    Validate.notNull(swap, "null VarianceSwap");
    Validate.notNull(market, "null VarianceSwapDataBundle");

    // Unpack market data
    final Surface<Double, Double, Double> surface = market.getVolatilitySurface().getSurface();
    Validate.isTrue(surface instanceof InterpolatedDoublesSurface,
        "Currently will only accept a Equity VolatilitySurfaces based on an InterpolatedDoublesSurface");

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) surface;
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
  public double calcBlackVegaForSinglePoint(final VarianceSwap swap, final VarianceSwapDataBundle market, final double maturity, final double strike, final double shift) {

    final VarianceSwapPresentValueCalculator pricer = VarianceSwapPresentValueCalculator.getInstance();

    final Surface<Double, Double, Double> surface = market.getVolatilitySurface().getSurface();
    Validate.isTrue(surface instanceof InterpolatedDoublesSurface,
        "Currently will only accept a Equity VolatilitySurfaces based on an InterpolatedDoublesSurface");

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) surface;
    final InterpolatedSurfaceAdditiveShiftFunction volShifter = new InterpolatedSurfaceAdditiveShiftFunction();

    // shift UP
    final InterpolatedDoublesSurface bumpedVolUp = volShifter.evaluate(blackSurf, maturity, strike, shift);
    VarianceSwapDataBundle bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface().withSurface(bumpedVolUp), market.getDiscountCurve(), market.getForwardCurve());
    final double pvUp = pricer.visitVarianceSwap(swap, bumpedMarket);

    // shift DOWN
    final InterpolatedDoublesSurface bumpedVolDown = volShifter.evaluate(blackSurf, maturity, strike, -shift);
    bumpedMarket = new VarianceSwapDataBundle(market.getVolatilitySurface().withSurface(bumpedVolDown), market.getDiscountCurve(), market.getForwardCurve());
    final double pvDown = pricer.visitVarianceSwap(swap, bumpedMarket);

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

    final int nObsExpected = swap.getObsExpected();
    final int nObsSoFar = swap.getObservations().length;
    final int nObsDidntHappen = swap.getObsDisrupted();

    return (nObsExpected - nObsSoFar - nObsDidntHappen) / nObsExpected * swap.getVarNotional();
  }

}
