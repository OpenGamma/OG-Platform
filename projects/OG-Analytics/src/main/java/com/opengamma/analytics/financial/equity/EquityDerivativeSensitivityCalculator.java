/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.GeneralSmileInterpolator;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneynessFcnBackedByGrid;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedSurfaceAdditiveShiftFunction;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Triple;

/**
 * This Calculator provides simple bump and reprice sensitivities for Derivatives
 */
@SuppressWarnings("deprecation")
public class EquityDerivativeSensitivityCalculator {
  /** The default value of the absolute shift */
  private static final double DEFAULT_ABS_SHIFT = 0.0001; // Shift used for vol, +/- 1bp == 0.01%
  /** The default value of the relative shift */
  private static final double DEFAULT_REL_SHIFT = 0.01; // Shift used for rates  +/- 1% * Rate
  /** Gets the settlement time for the instrument */
  private static final SettlementTimeCalculator SETTLEMENT_CALCULATOR = SettlementTimeCalculator.getInstance();
  /** The pricer */
  private final InstrumentDerivativeVisitor<StaticReplicationDataBundle, Double> _pricer;

  /**
   * @param pricer The pricer, not null
   */
  public EquityDerivativeSensitivityCalculator(final InstrumentDerivativeVisitor<StaticReplicationDataBundle, Double> pricer) {
    ArgumentChecker.notNull(pricer, "pricer");
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
  public Double calcForwardSensitivity(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double relShift) {
    ArgumentChecker.notNull(derivative, "null EquityDerivative");
    ArgumentChecker.notNull(market, "null EquityOptionDataBundle");

    // Shift UP
    StaticReplicationDataBundle bumpedMarket = new StaticReplicationDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve()
        .withFractionalShift(relShift));
    final double pvUp = derivative.accept(_pricer, bumpedMarket);

    // Shift Down
    bumpedMarket = new StaticReplicationDataBundle(market.getVolatilitySurface(), market.getDiscountCurve(), market.getForwardCurve().withFractionalShift(-relShift));
    final double pvDown = derivative.accept(_pricer, bumpedMarket);

    final double t = derivative.accept(SETTLEMENT_CALCULATOR);
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
  public Double calcForwardSensitivity(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
    return calcForwardSensitivity(derivative, market, DEFAULT_REL_SHIFT);
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
  public Double calcDiscountRateSensitivity(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double shift) {
    ArgumentChecker.notNull(market, "market");
    ArgumentChecker.notNull(derivative, "derivative");
    // Sensitivity from the discounting

    final double pv = derivative.accept(_pricer, market);
    final double timeToSettlement = derivative.accept(SETTLEMENT_CALCULATOR);

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
  public Double calcDiscountRateSensitivity(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
    return calcDiscountRateSensitivity(derivative, market, DEFAULT_REL_SHIFT);
  }

  /**
   * Calculates the sensitivity of the present value (PV) to a basis point (bp) move in the funding rates across all maturities. <p>
   * Also know as PVBP and DV01.
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double in the currency, derivative.getCurrency()
   */
  public Double calcPV01(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
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
  public DoubleMatrix1D calcDeltaBucketed(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
    ArgumentChecker.notNull(derivative, "null EquityDerivative");
    ArgumentChecker.notNull(market, "null EquityOptionDataBundle");

    // We know that the EquityDerivative only has true sensitivity to one maturity on one curve.
    // A function written for interestRate sensitivities spreads this sensitivity across yield nodes
    // NodeSensitivityCalculator.curveToNodeSensitivities(curveSensitivities, interpolatedCurves)

    if (!(market.getDiscountCurve() instanceof YieldCurve)) {
      throw new IllegalArgumentException("Can only handle YieldCurve");
    }
    final YieldCurve discCrv = (YieldCurve) market.getDiscountCurve();

    final double settlement = derivative.accept(SETTLEMENT_CALCULATOR);
    final double sens = calcDiscountRateSensitivity(derivative, market);

    final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    final List<Double> result = distributor.curveToNodeSensitivity(Arrays.asList(DoublesPair.of(settlement, sens)), discCrv);
    return new DoubleMatrix1D(result.toArray(new Double[result.size()]));
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatilities at the knot points of the surface. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
    return calcBlackVegaParallel(derivative, market, DEFAULT_ABS_SHIFT);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatilities at the knot points of the surface. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param shift Size of shift made in centered-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A Double. Currency amount per unit amount change in the black volatility
   */
  public Double calcBlackVegaParallel(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double shift) {
    ArgumentChecker.notNull(derivative, "null EquityDerivative");
    ArgumentChecker.notNull(market, "null EquityOptionDataBundle");

    // Parallel shift UP
    final BlackVolatilitySurface<?> upSurface = market.getVolatilitySurface().withShift(shift, true);
    final double pvUp = derivative.accept(_pricer, new StaticReplicationDataBundle(upSurface, market.getDiscountCurve(), market.getForwardCurve()));

    // Parallel shift DOWN
    final BlackVolatilitySurface<?> downSurface = market.getVolatilitySurface().withShift(-shift, true);
    final double pvDown = derivative.accept(_pricer, new StaticReplicationDataBundle(downSurface, market.getDiscountCurve(), market.getForwardCurve()));

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatilities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currency amount per unit amount change in the black volatility of each node
   */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final InstrumentDerivative derivative, final StaticReplicationDataBundle market) {
    return calcBlackVegaForEntireSurface(derivative, market, DEFAULT_ABS_SHIFT);
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the lognormal Black implied volatilities at the knot points of the surface. <p>
   * The return format is a DoubleMatrix2D with rows equal to the total number of maturities and columns equal to the number of strikes. <p>
   * Note - the change of the surface due to the movement of a single node is interpolator-dependent, so an instrument may have non-local sensitivity
   * @param derivative the EquityDerivative
   * @param market the EquityOptionDataBundle
   * @param shift Size of shift made in centred-finite difference approximation. e.g. 1% would be 0.01, and 1bp 0.0001
   * @return A NodalDoublesSurface with same axes as market.getVolatilitySurface(). Contains currency amount per unit amount change in the black volatility of each node
   */
  public NodalDoublesSurface calcBlackVegaForEntireSurface(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double shift) {
    ArgumentChecker.notNull(derivative, "null EquityDerivative");
    ArgumentChecker.notNull(market, "null EquityOptionDataBundle");

    if (market.getVolatilitySurface().getSurface() instanceof InterpolatedDoublesSurface) {
      final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) market.getVolatilitySurface().getSurface();
      final Double[] maturities = blackSurf.getXData();
      final Double[] strikes = blackSurf.getYData();
      final int nNodes = maturities.length;
      ArgumentChecker.isTrue(nNodes == strikes.length, "Number of strikes must match number of nodes");
      // Bump and reprice
      final Double[] vegas = new Double[nNodes];
      for (int j = 0; j < nNodes; j++) {
        vegas[j] = calcBlackVegaForSinglePoint(derivative, market, maturities[j], strikes[j], shift);
      }
      return NodalDoublesSurface.from(maturities, strikes, vegas);

      // Special case for EquityIndexOptions
    } else if (market.getVolatilitySurface() instanceof BlackVolatilitySurfaceMoneynessFcnBackedByGrid) {
      final BlackVolatilitySurfaceMoneynessFcnBackedByGrid surfaceBundle = (BlackVolatilitySurfaceMoneynessFcnBackedByGrid) market.getVolatilitySurface();

      final EquityIndexOption option;
      if (derivative instanceof EquityIndexOption) {
        option = (EquityIndexOption) derivative;
      } else {
        throw new OpenGammaRuntimeException("Calculator with BlackVolatilitySurfaceMoneynessFcnBackedByGrid was expecting an EquityIndexOption.");
      }
      // Unpack
      final SmileSurfaceDataBundle volGrid = surfaceBundle.getGridData();
      final double[] forwards = volGrid.getForwards();
      final double[] volExpiries = volGrid.getExpiries();
      final int nExpiries = volGrid.getNumExpiries();
      final double[][] strikes = volGrid.getStrikes();
      final double[][] vols = volGrid.getVolatilities();
      final VolatilitySurfaceInterpolator surfaceInterpolator = surfaceBundle.getInterpolator();
      final GeneralSmileInterpolator strikeInterpolator = surfaceInterpolator.getSmileInterpolator();

      // Base price and set of independent smile fits (one function vol(k) for each expiry)
      final Double pvBase = option.accept(_pricer, market);
      final Function1D<Double, Double>[] smileFitsBase = surfaceInterpolator.getIndependentSmileFits(volGrid);

      // Bump and reprice - loop over expiry and strike
      final List<Triple<Double, Double, Double>> triplesExpiryStrikeVega = new ArrayList<>();
      // TODO: REVIEW: We can drastically reduce the time it takes to compute this if we are sensible about avoiding points which almost certainly won't have any sensitivity
      // Of course, this is all based upon the interpolor's scheme...
      final int expiryIndex = SurfaceArrayUtils.getLowerBoundIndex(volExpiries, option.getTimeToExpiry());
      for (int t = Math.max(0, expiryIndex - 3); t < Math.min(nExpiries, expiryIndex + 4); t++) {
        final int nStrikes = strikes[t].length;
        final int strikeIndex = SurfaceArrayUtils.getLowerBoundIndex(strikes[t], option.getStrike());
        for (int k = Math.max(0, strikeIndex - 6); k < Math.min(nStrikes, strikeIndex + 7); k++) {
          // TODO: REVIEW We only recompute the smile function for the specific expiry we are bumping..
          final double[] bumpedVols = Arrays.copyOf(vols[t], nStrikes);
          bumpedVols[k] = vols[t][k] - shift;
          final Function1D<Double, Double> thisExpirysSmile = strikeInterpolator.getVolatilityFunction(forwards[t], strikes[t], volExpiries[t], bumpedVols);
          final Function1D<Double, Double>[] scenarioSmileFits = Arrays.copyOf(smileFitsBase, smileFitsBase.length);
          scenarioSmileFits[t] = thisExpirysSmile;
          final BlackVolatilitySurfaceMoneynessFcnBackedByGrid shiftedSurface = surfaceInterpolator.combineIndependentSmileFits(scenarioSmileFits, volGrid);
          //BlackVolatilitySurfaceMoneynessFcnBackedByGrid shiftedSurface = surfaceInterpolator.getBumpedVolatilitySurface(volGrid, t, k, -shift);
          final StaticReplicationDataBundle shiftedMarket = market.withShiftedSurface(shiftedSurface);
          final Double pvScenario = option.accept(_pricer, shiftedMarket);

          ArgumentChecker.notNull(pvScenario, "Null PV in shifted scenario, T = " + volExpiries[t] + ", k = " + strikes[t][k]);
          final Double vega = (pvScenario - pvBase) / -shift;
          final Triple<Double, Double, Double> xyz = new Triple<>(volExpiries[t], strikes[t][k], vega);
          triplesExpiryStrikeVega.add(xyz);
        }
      }
      return NodalDoublesSurface.from(triplesExpiryStrikeVega);

    } else {
      throw new OpenGammaRuntimeException("Currently will only accept an Equity Volatility Surface based on an InterpolatedDoublesSurface, "
          + "or BlackVolatilitySurfaceMoneynessFcnBackedByGrid");
    }
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
  public double calcBlackVegaForSinglePoint(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double maturity, final double strike,
      final double shift) {

    final Surface<Double, Double, Double> surface = market.getVolatilitySurface().getSurface();
    ArgumentChecker.isTrue(surface instanceof InterpolatedDoublesSurface, "Currently will only accept a Equity VolatilitySurfaces based on an InterpolatedDoublesSurface");

    final InterpolatedDoublesSurface blackSurf = (InterpolatedDoublesSurface) surface;
    final InterpolatedSurfaceAdditiveShiftFunction volShifter = new InterpolatedSurfaceAdditiveShiftFunction();

    // shift UP
    final InterpolatedDoublesSurface bumpedVolUp = volShifter.evaluate(blackSurf, maturity, strike, shift);
    StaticReplicationDataBundle bumpedMarket = new StaticReplicationDataBundle(market.getVolatilitySurface().withSurface(bumpedVolUp), market.getDiscountCurve(),
        market.getForwardCurve());
    final double pvUp = derivative.accept(_pricer, bumpedMarket);

    // shift DOWN
    final InterpolatedDoublesSurface bumpedVolDown = volShifter.evaluate(blackSurf, maturity, strike, -shift);
    bumpedMarket = new StaticReplicationDataBundle(market.getVolatilitySurface().withSurface(bumpedVolDown), market.getDiscountCurve(), market.getForwardCurve());
    final double pvDown = derivative.accept(_pricer, bumpedMarket);

    // Centered-difference result
    return (pvUp - pvDown) / (2.0 * shift);
  }

}
