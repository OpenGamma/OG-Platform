/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.interestrate.NodeYieldSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public final class EquityFuturesRatesSensitivityCalculator {
  private static final double SHIFT = 0.01;
  private static final EquityFuturesRatesSensitivityCalculator s_instance = new EquityFuturesRatesSensitivityCalculator();

  public static EquityFuturesRatesSensitivityCalculator getInstance() {
    return s_instance;
  }

  private EquityFuturesRatesSensitivityCalculator() {
  }

  /**
   * This calculates the sensitivity of the present value (PV) to the continuously-compounded discount rates at the knot points of the funding curve. <p>
   * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in the curve <p>
   * The change of a curve due to the movement of a single knot is interpolator-dependent, so an instrument can have sensitivity to knots at times beyond its maturity
   * @param future the EquityDerivative
   * @param dataBundle the EquityFutureDataBundle
   * @return A DoubleMatrix1D containing bucketed delta in order and length of market.getDiscountCurve(). Currency amount per unit amount change in discount rate
   */
  public DoubleMatrix1D calcDeltaBucketed(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    if (!(dataBundle.getFundingCurve() instanceof YieldCurve)) {
      throw new IllegalArgumentException("Calculator expects a YieldCurve. Perhaps it has encountered a discount curve?");
    }
    final YieldCurve discCrv = (YieldCurve) dataBundle.getFundingCurve();
    final String discCrvName = discCrv.getCurve().getName();
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle();
    interpolatedCurves.setCurve(discCrvName, discCrv);
    final double settlement = future.getTimeToSettlement();
    final EquityFuturesPresentValueCalculator pricer = EquityFuturesPresentValueCalculator.getInstance();
    SimpleFutureDataBundle bumpedMarket = new SimpleFutureDataBundle(discCrv.withSingleShift(settlement, SHIFT), dataBundle.getMarketPrice(),
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvUp = future.accept(pricer, bumpedMarket);
    bumpedMarket = new SimpleFutureDataBundle(discCrv.withSingleShift(settlement, -SHIFT), dataBundle.getMarketPrice(),
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvDown = future.accept(pricer, bumpedMarket);
    final double sensitivity = (pvUp - pvDown) / (2.0 * SHIFT);
    final Map<String, List<DoublesPair>> curveSensitivities = new HashMap<String, List<DoublesPair>>();
    curveSensitivities.put(discCrvName, Lists.newArrayList(new DoublesPair(settlement, sensitivity)));
    final NodeYieldSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

}
