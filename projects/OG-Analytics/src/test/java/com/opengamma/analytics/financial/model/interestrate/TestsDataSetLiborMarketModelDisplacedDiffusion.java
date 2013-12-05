/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 * Data used for tests on the LMM displaced diffusion.
 */
public class TestsDataSetLiborMarketModelDisplacedDiffusion {

  private static final double MEAN_REVERSION = 0.001;
  private static final double DISPLACEMENT = 0.10;
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;

  /**
   * Create LMM parameters adapted to a given swap with the test data.
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParameters(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, annuity, IBOR_DAY_COUNT, DISPLACEMENT, MEAN_REVERSION, new VolatilityLMM());
  }

  /**
   * Create LMM parameters adapted to a given swap with the test data. The volatilities are shifted by a given amount.
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParametersShiftVol(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity,
      final double shift) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, annuity, IBOR_DAY_COUNT, DISPLACEMENT, MEAN_REVERSION, new VolatilityLMM(shift));
  }

  /**
   * Create LMM parameters adapted to a given swap with the test data. The displacements are shifted by a given amount.
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParametersShiftDis(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity,
      final double shift) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, annuity, IBOR_DAY_COUNT, DISPLACEMENT + shift, MEAN_REVERSION, new VolatilityLMM());
  }

  /**
   * Create LMM parameters adapted to a given swap with the test data. The two factors weights are created from an "angle".
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @param angle The "angle" defining the weights. The two factors weights are cos(angle*t/20) and sin(angle*t/20).
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParametersAngle(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity,
      final double angle) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, annuity, IBOR_DAY_COUNT, DISPLACEMENT, MEAN_REVERSION, new VolatilityLMMAngle(angle));
  }

  /**
   * Create LMM parameters adapted to a given swap with the test data. The displacement is provided. The two factors weights are created from an "angle".
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @param displacement The displacement (common to all periods).
   * @param angle The "angle" defining the weights. The two factors weights are cos(angle*t/20) and sin(angle*t/20).
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParametersDisplacementAngle(final ZonedDateTime modelDate, final AnnuityCouponDefinition<? extends CouponDefinition> annuity,
      final double displacement, final double angle) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, annuity, IBOR_DAY_COUNT, displacement, MEAN_REVERSION, new VolatilityLMMAngle(angle));
  }

}

class VolatilityLMM extends Function1D<Double, Double[]> {

  private final double _shift;

  public VolatilityLMM() {
    _shift = 0.0;
  }

  public VolatilityLMM(double shift) {
    _shift = shift;
  }

  @Override
  public Double[] evaluate(Double x) {
    Double[] result = new Double[2];
    result[0] = 0.06 + _shift;
    result[1] = -0.06 + x * 0.006 + _shift;
    return result;
  }

}

class VolatilityLMMAngle extends Function1D<Double, Double[]> {

  private final double _shift;
  /**
   * The angle between the factors: factor 1 weight is cos(angle*t/20) and factor 2 weight is sin(angle*t/20).
   * For the angle = 0, there is only one factor. For angle = pi/2, the 0Y rate is independent of the 20Y rate.
   */
  private final double _angle;

  public VolatilityLMMAngle(double angle) {
    _shift = 0.0;
    _angle = angle;
  }

  public VolatilityLMMAngle(double angle, double shift) {
    _shift = shift;
    _angle = angle;
  }

  @Override
  public Double[] evaluate(Double x) {
    Double[] result = new Double[2];
    result[0] = 0.06 * Math.cos(x / 20.0 * _angle) + _shift;
    result[1] = 0.06 * Math.sin(x / 20.0 * _angle) + _shift;
    return result;
  }

}
