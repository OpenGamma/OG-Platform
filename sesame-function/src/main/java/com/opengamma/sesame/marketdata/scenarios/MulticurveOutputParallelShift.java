/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurveUtils;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.marketdata.MarketDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Applies a parallel shift to a calibrated curve.
 * <p>
 * Shifts can be {@link #absolute} or {@link #relative}. An absolute shift adds the same amount to the value
 * at every curve node. A relative shift multiplies the value at each curve node by the same factor.
 */
public class MulticurveOutputParallelShift implements Perturbation {

  private final ShiftType _shiftType;
  private final double _shiftAmount;

  private MulticurveOutputParallelShift(ShiftType shiftType, double shiftAmount) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shiftAmount = shiftAmount;
  }

  /**
   * Creates a shift that adds a fixed amount to the value at every node in the curve.
   *
   * @param shiftAmount the amount to add to each node value in the curve
   * @return a shift that adds a fixed amount to the value at every node in the curve
   */
  public static MulticurveOutputParallelShift absolute(double shiftAmount) {
    return new MulticurveOutputParallelShift(ShiftType.ABSOLUTE, shiftAmount);
  }

  /**
   * Creates a shift that multiplies the values at each curve node by a fixed factor.
   *
   * @param shiftAmount the factor to multiply the value at each curve node by
   * @return a shift that multiplies the values at each curve node by a fixed factor
   */
  public static MulticurveOutputParallelShift relative(double shiftAmount) {
    return new MulticurveOutputParallelShift(ShiftType.RELATIVE, shiftAmount);
  }

  /**
   * Applies the shift to the curve.
   *
   * @param marketData a piece of market data with type {@link MulticurveBundle}
   * @param matchDetails details of the match which the {@link MarketDataFilter} was applied to the market data
   * @return the shifted curve
   */
  @Override
  public MulticurveBundle apply(Object marketData, MatchDetails matchDetails) {
    MulticurveBundle bundle = ((MulticurveBundle) marketData);
    String curveName = ((MulticurveMatchDetails) matchDetails).getCurveName();
    // This is safe ATM, all curves are YieldCurves. this will need to be updated when that changes.
    // As part of that an equivalent of YieldCurveUtils.withParallelShift will be required for discount curves
    MulticurveProviderDiscount multicurve = bundle.getMulticurveProvider();
    YieldCurve curve = (YieldCurve) multicurve.getCurve(curveName);
    YieldCurve shiftedCurve = YieldCurveUtils.withParallelShift(curve, _shiftAmount, _shiftType, "");
    MulticurveProviderDiscount shiftedMulticurve = MarketDataUtils.replaceCurve(multicurve, shiftedCurve);
    // If the original block bundle were reused, any sensitivity calcs using the curve would be wrong because
    // they would be relative to the unshifted curve. Deriving a new block bundle from the shifted curve
    // isn't practical. But apparently (according to Marc) it's a reasonable limitation - you shouldn't
    // expect to be able to shock a curve and get out the sensitivities to the shocked curve.
    // Providing an empty block bundle satisfies MulticurveBundle and ensures that sensitivity calculations
    // will fail.
    return new MulticurveBundle(shiftedMulticurve, new CurveBuildingBlockBundle());
  }

  @Override
  public Class<MulticurveBundle> getMarketDataType() {
    return MulticurveBundle.class;
  }

  @Override
  public Class<? extends MatchDetails> getMatchDetailsType() {
    return MulticurveMatchDetails.class;
  }

  @Override
  public PerturbationTarget getTargetType() {
    return PerturbationTarget.OUTPUT;
  }
}
