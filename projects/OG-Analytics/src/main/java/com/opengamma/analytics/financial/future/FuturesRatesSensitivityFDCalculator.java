/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.future;

import java.util.Arrays;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 *
 */
@SuppressWarnings("deprecation")
public final class FuturesRatesSensitivityFDCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<SimpleFutureDataBundle, DoubleMatrix1D> {
  private static final double SHIFT = 0.01;
  private static final SettlementTimeCalculator SETTLEMENT_TIME = SettlementTimeCalculator.getInstance();
  private final InstrumentDerivativeVisitor<SimpleFutureDataBundle, Double> _presentValueCalculator;

  public static FuturesRatesSensitivityFDCalculator getInstance(final InstrumentDerivativeVisitor<SimpleFutureDataBundle, Double> presentValueCalculator) {
    return new FuturesRatesSensitivityFDCalculator(presentValueCalculator);
  }

  private FuturesRatesSensitivityFDCalculator(final InstrumentDerivativeVisitor<SimpleFutureDataBundle, Double> presentValueCalculator) {
    ArgumentChecker.notNull(presentValueCalculator, "present value calculator");
    _presentValueCalculator = presentValueCalculator;
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the continuously-compounded discount rates at the knot points of the funding curve. <p>
   * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in the curve <p>
   * The change of a curve due to the movement of a single knot is interpolator-dependent, so an instrument can have sensitivity to knots at times beyond its maturity
   * @param future the derivative, not null
   * @param dataBundle the data bundle, not null
   * @return A DoubleMatrix1D containing bucketed delta in order and length of market.getDiscountCurve(). Currency amount per unit amount change in discount rate
   */
  @Override
  public DoubleMatrix1D visit(final InstrumentDerivative future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    ArgumentChecker.isTrue(dataBundle.getFundingCurve() instanceof YieldCurve, "Calculator expects a YieldCurve, have {}", dataBundle.getFundingCurve().getClass());
    final YieldCurve discCrv = (YieldCurve) dataBundle.getFundingCurve();
    final double settlement = future.accept(SETTLEMENT_TIME);
    SimpleFutureDataBundle bumpedMarket = new SimpleFutureDataBundle(discCrv.withSingleShift(settlement, SHIFT), dataBundle.getMarketPrice(),
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvUp = future.accept(_presentValueCalculator, bumpedMarket);
    bumpedMarket = new SimpleFutureDataBundle(discCrv.withSingleShift(settlement, -SHIFT), dataBundle.getMarketPrice(),
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvDown = future.accept(_presentValueCalculator, bumpedMarket);
    final double sensitivity = (pvUp - pvDown) / (2.0 * SHIFT);
    final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    final List<Double> result = distributor.curveToNodeSensitivity(Arrays.asList(DoublesPair.of(settlement, sensitivity)), discCrv);
    return new DoubleMatrix1D(result.toArray(new Double[result.size()]));
  }

  @Override
  public DoubleMatrix1D visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need a SimpleFutureDataBundle to calculates bucketed delta");
  }

}
