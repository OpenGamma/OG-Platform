/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.AddCurveSpreadFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
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
    YieldCurve shiftedCurve = parallelShift(curve, _shiftAmount, _shiftType);
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

  // TODO this was copy-pasted from YieldCurveUtils because that renames the curve. unify
  /**
   * Shifts a curve by a constant amount over all tenors. If the {@link ShiftType} is
   * absolute, the shift is added to the curve i.e. a shift of 0.0001 results in all
   * yields on the curve having one basis point added. If it is relative, then all yields on
   * are the curve are multiplied by the shift amount i.e. a relative shift of 0.01 will
   * result in all points on the curve being shifted upwards by 1% of the yield.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   *
   * @param curve the original curve
   * @param shift the shift
   * @param shiftType the shift type
   * @return a new curve with all values shifted by a constant amount
   */
  private static YieldCurve parallelShift(YieldCurve curve, double shift, ShiftType shiftType) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(shiftType, "shift type");

    DoublesCurve underlyingCurve = curve.getCurve();
    String name = curve.getName();

    switch (shiftType) {
      case ABSOLUTE:
        ConstantDoublesCurve constantCurve = ConstantDoublesCurve.from(shift);
        SpreadDoublesCurve absShiftedCurve =
            SpreadDoublesCurve.from(
                AddCurveSpreadFunction.getInstance(),
                name,
                underlyingCurve,
                constantCurve);
        return new YieldCurve(name, absShiftedCurve);
      case RELATIVE:
        ConstantDoublesCurve constantDoublesCurve = ConstantDoublesCurve.from(1 + shift);
        SpreadDoublesCurve relShiftedCurve =
            SpreadDoublesCurve.from(
                MultiplyCurveSpreadFunction.getInstance(),
                name,
                underlyingCurve,
                constantDoublesCurve);
        return new YieldCurve(name, relShiftedCurve);
      default:
        throw new IllegalArgumentException("Cannot handle curve shift type " + shiftType + " for parallel shifts");
    }
  }
}
