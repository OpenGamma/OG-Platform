/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;
import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.financial.interestrate.NodeSensitivityCalculator;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.matrix.DoubleMatrix1D;
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
  public DoubleMatrix1D calcDeltaBucketed(final EquityFuture future, final EquityFutureDataBundle dataBundle) {
    Validate.notNull(future, "null future");
    Validate.notNull(dataBundle, "null data bundle");
    final YieldAndDiscountCurve discCrv = dataBundle.getFundingCurve();
    final String discCrvName = discCrv.getCurve().getName();
    final YieldCurveBundle interpolatedCurves = new YieldCurveBundle();
    interpolatedCurves.setCurve(discCrvName, discCrv);
    final double settlement = future.getTimeToSettlement();
    final EquityFuturesPresentValueCalculator pricer = EquityFuturesPresentValueCalculator.getInstance();
    EquityFutureDataBundle bumpedMarket = new EquityFutureDataBundle(dataBundle.getFundingCurve().withSingleShift(settlement, SHIFT), dataBundle.getMarketPrice(), 
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvUp = pricer.visit(future, bumpedMarket);
    bumpedMarket = new EquityFutureDataBundle(dataBundle.getFundingCurve().withSingleShift(settlement, -SHIFT), dataBundle.getMarketPrice(), 
        dataBundle.getSpotValue(), dataBundle.getDividendYield(), dataBundle.getCostOfCarry());
    final double pvDown = pricer.visit(future, bumpedMarket);
    final double sensitivity = (pvUp - pvDown) / (2.0 * SHIFT);
    final Map<String, List<DoublesPair>> curveSensitivities = new HashMap<String, List<DoublesPair>>();
    curveSensitivities.put(discCrvName, Lists.newArrayList(new DoublesPair(settlement, sensitivity)));
    final NodeSensitivityCalculator distributor = PresentValueNodeSensitivityCalculator.getDefaultInstance();
    return distributor.curveToNodeSensitivities(curveSensitivities, interpolatedCurves);
  }

}
