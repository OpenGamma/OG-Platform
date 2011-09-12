/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.math.function.Function1D;

/**
 * Data used for tests on the LMM displaced diffusion.
 */
public class LiborMarketModelDisplacedDiffusionTestsDataSet {

  private static final double MEAN_REVERSION = 0.01;
  private static final double DISPLACEMENT = 0.10;
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");

  /**
   * Create LMM parameters adapted to a given swap with the test data.
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParameters(final ZonedDateTime modelDate, final SwapFixedIborDefinition swap) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, swap, IBOR_DAY_COUNT, DISPLACEMENT, MEAN_REVERSION, new VolatilityLMM());
  }

  /**
   * Create LMM parameters adapted to a given swap with the test data.
   * @param modelDate The pricing date.
   * @param swap The swap.
   * @return The LMM parameters.
   */
  public static LiborMarketModelDisplacedDiffusionParameters createLMMParameters(final ZonedDateTime modelDate, final SwapFixedIborDefinition swap, final double shift) {
    return LiborMarketModelDisplacedDiffusionParameters.from(modelDate, swap, IBOR_DAY_COUNT, DISPLACEMENT, MEAN_REVERSION, new VolatilityLMM(shift));
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
    result[0] = 0.02 + _shift;
    result[1] = -0.02 + x * 0.002 + _shift;
    return result;
  }

}
