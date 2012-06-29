/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.google.common.collect.Lists;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

/**
 * This Calculator provides simple bump and reprice sensitivities for EquityDerivatives
 */
public class EquityDerivativeSensitivityCalculator {

  private final AbstractEquityDerivativeVisitor<EquityOptionDataBundle, Double> _pricer;

  public EquityDerivativeSensitivityCalculator(final AbstractEquityDerivativeVisitor<EquityOptionDataBundle, Double> pricer) {
    _pricer = pricer;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to a unit move in the forward.
   * The volatility surface remains unchanged.
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param relShift Relative size of shift made in centered-finite difference approximation.
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcForwardSensitivity(final EquityDerivative derivative, final EquityOptionDataBundle market, final double relShift) {
    Validate.notNull(derivative, "null EquityDerivative");
    Validate.notNull(market, "null EquityOptionDataBundle");

    // Shift UP
    EquityOptionDataBundle bumpedMarket = new EquityOptionDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve().withFractionalShift(relShift));
    final double pvUp = _pricer.visit(derivative, bumpedMarket);

    // Shift Down
    bumpedMarket = new EquityOptionDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve().withFractionalShift(-relShift));
    final double pvDown = _pricer.visit(derivative, bumpedMarket);

    final double t = derivative.getTimeToSettlement();
    final double fwd = market.getForwardCurve().getForward(t);
    // Centered-difference result
    return (pvUp - pvDown) / 2.0 / relShift / fwd;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to a unit move in the forward,
   * under a default shift of 1% of the forward <p>
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcForwardSensitivity(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    final double relativeShift = 0.01;
    return calcForwardSensitivity(derivative, market, relativeShift);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also know as PVBP and DV01, though note this return per UNIT change in rate. calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a EquityDerivative in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = -(T-t)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param shift Relative size of shift made in centered-finite difference approximation.
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate
   */
  public Double calcDiscountRateSensitivity(final EquityDerivative derivative, final EquityOptionDataBundle market, final double shift) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    // Sensitivity from the discounting

    final double pv = _pricer.visit(derivative, market);
    final double timeToSettlement = derivative.getTimeToSettlement();

    // Sensitivity from forward projection
    final double fwdSens = calcForwardSensitivity(derivative, market, shift);
    final double fwd = market.getForwardCurve().getForward(timeToSettlement);

    return timeToSettlement * (fwd * fwdSens - pv);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a change in the funding rate from valuation to settlement.
   * Also know as PVBP and DV01, though note this return per UNIT change in rate. calcPV01 returns per basis point change in rates.  <p>
   * <p>
   * Rates enter the pricing of a EquityDerivative in two places: in the discounting and forward projection.<p>
   * The presentValue has been structured such that the form of the PV = Z(t,T) * FwdPrice(t,T) with Z a zero coupon bond, and t and T the valuation and settlement times respectively.
   * The form of our discounting rates is such that Z(t,T) = exp[- R(t,T) * (T-t)], hence  dZ/dR = (t-T)*Z(t,T) and d(PV)/dR = PV * dZ/dR
   * The forward's dependence on the discounting rate is similar to the zero coupon bonds, but of opposite sign, dF/dR = (T-t)*F(t,T)
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double in the currency, deriv.getCurrency(). Currency amount per unit amount change in discount rate
   */
  public Double calcDiscountRateSensitivity(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    final double relativeShift = 0.01;
    return calcDiscountRateSensitivity(derivative, market, relativeShift);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a basis point (bp) move in the funding rates across all maturities. <p>
   * Also know as PVBP and DV01.
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double in the currency, derivative.getCurrency()
   */
  public Double calcPV01(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    return calcDiscountRateSensitivity(derivative, market) / 10000;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the continuously-compounded discount rates at the knot points of the funding curve. <p>
   * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in the curve <p>
   * The change of a curve due to the movement of a single knot is interpolator-dependent, so an instrument can have sensitivity to knots at times beyond its maturity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A DoubleMatrix1D containing bucketed delta in order and length of market.getDiscountCurve(). Currency amount per unit amount change in discount rate
   */
  public DoubleMatrix1D calcDeltaBucketed(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    Validate.notNull(derivative, "null EquityDerivative");
    Validate.notNull(market, "null EquityOptionDataBundle");

    // We know that the EquityDerivative only has true sensitivity to one maturity on one curve.
    // A function written for interestRate sensitivities spreads this sensitivity across yield nodes
    // NodeSensitivityCalculator.curveToNodeSensitivities(curveSensitivities, interpolatedCurves)

    // 2nd arg = LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves
    final YieldAndDiscountCurve discCrv = market.getDiscountCurve();
    final String discCrvName = discCrv.getCurve().getName();
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle();
    interpolatedCurves.setCurve(discCrvName, discCrv);

    // 1st arg = Map<String, List<DoublesPair>> curveSensitivities = <curveName, List<(maturity,sensitivity)>>
    final double settlement = derivative.getTimeToSettlement();
    final Double sens = calcDiscountRateSensitivity(derivative, market);
    final Map<String, List<DoublesPair>> curveSensitivities = new HashMap<String, List<DoublesPair>>();
    curveSensitivities.put(discCrvName, Lists.newArrayList(new DoublesPair(settlement, sens)));

    final NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    final double shift = 0.001; // Shift each vol point by what? +/- 0.1%
    return calcBlackVegaParallel(derivative, market, shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final EquityDerivative derivative, final EquityOptionDataBundle market, final double shift) {
    Validate.notNull(derivative, "null EquityDerivative");
    Validate.notNull(market, "null EquityOptionDataBundle");

    // Parallel shift UP
    final BlackVolatilitySurface<?> upSurface = market.getVolatilitySurface().withShift(shift, true);
    final double pvUp = _pricer.visit(derivative, new EquityOptionDataBundle(upSurface, market.getDiscountCurve(), market.getForwardCurve()));

    // Parallel shift DOWN
    final BlackVolatilitySurface<?> downSurface = market.getVolatilitySurface().withShift(-shift, true);
    final double pvDown = _pricer.visit(derivative, new EquityOptionDataBundle(downSurface, market.getDiscountCurve(), market.getForwardCurve()));

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currencys amount per unit amount change in the black volatility of each node
   */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final EquityDerivative derivative, final EquityOptionDataBundle market) {
    final double shift = 0.001; // Shift each vol point by what? +/- 0.1%
    return calcBlackVegaForEntireSurface(derivative, market, shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currency amount per unit amount change in the black volatility of each node
   */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final EquityDerivative derivative, final EquityOptionDataBundle market, final double shift) {
    Validate.notNull(derivative, "null EquityDerivative");
    Validate.notNull(market, "null EquityOptionDataBundle");

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
      vegas[j] = calcBlackVegaForSinglePoint(derivative, market, maturities[j], strikes[j], shift);
    }

    return new NodalDoublesSurface(maturities, strikes, vegas);
  }

  /**
   * Compute the price sensitivity to a shift of the Black volatility at a given maturity and strike. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity.<p>
   * Important!!! If the <i>(x, y)</i> value(s) of the shift(s) are not in the nodal points of the original surface, they are added (with shift) to the nodal points of the new surface.
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param maturity a double in same unit as VolatilitySurface
   * @param strike a double in same unit as VolatilitySurface
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return Currency amount per unit amount change in the black volatility at the point provided
   */
  public double calcBlackVegaForSinglePoint(final EquityDerivative derivative, final EquityOptionDataBundle market, final double maturity, final double strike, final double shift) {

    final Surface<Double, Double, Double> surface = market.getVolatilitySurface().getSurface();
    Validate.isTrue(surface instanceof InterpolatedDoublesSurface,
        "Currently will only accept a Equity VolatilitySurfaces based on an InterpolatedDoublesSurface");

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) surface;
    final InterpolatedSurfaceAdditiveShiftFunction volShifter = new InterpolatedSurfaceAdditiveShiftFunction();

    // shift UP
    final InterpolatedDoublesSurface bumpedVolUp = volShifter.evaluate(blackSurf, maturity, strike, shift);
    EquityOptionDataBundle bumpedMarket = new EquityOptionDataBundle(market.getVolatilitySurface().withSurface(bumpedVolUp), market.getDiscountCurve(), market.getForwardCurve());
    final double pvUp = _pricer.visit(derivative, bumpedMarket);

    // shift DOWN
    final InterpolatedDoublesSurface bumpedVolDown = volShifter.evaluate(blackSurf, maturity, strike, -shift);
    bumpedMarket = new EquityOptionDataBundle(market.getVolatilitySurface().withSurface(bumpedVolDown), market.getDiscountCurve(), market.getForwardCurve());
    final double pvDown = _pricer.visit(derivative, bumpedMarket);

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  protected AbstractEquityDerivativeVisitor<EquityOptionDataBundle, Double> getPricer() {
    return _pricer;
  }
}
