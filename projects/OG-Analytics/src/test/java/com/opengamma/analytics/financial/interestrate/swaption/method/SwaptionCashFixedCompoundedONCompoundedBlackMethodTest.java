/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedCompoundedONCompoundingDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedCompoundedONCompoundedBlackMethodTest {

  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 9, 25);

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesBRL();
  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionBRL();
  private static final YieldCurveWithBlackSwaptionBundle CURVES_BLACK = new YieldCurveWithBlackSwaptionBundle(BLACK, CURVES);
  private static final String[] CURVES_NAME = TestsDataSetsBlack.curvesBRLNames();
  private static final Calendar CALENDAR = ((GeneratorSwapFixedCompoundedONCompounded) BLACK.getGeneratorSwap()).getOvernightCalendar();

  private static final GeneratorSwapFixedCompoundedONCompounded GENERATOR_OIS_BRL = (GeneratorSwapFixedCompoundedONCompounded) BLACK.getGeneratorSwap();

  private static final Period EXPIRY_TENOR = Period.ofMonths(26); // To be between nodes.
  private static final ZonedDateTime EXPIRY_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, EXPIRY_TENOR, GENERATOR_OIS_BRL.getBusinessDayConvention(), CALENDAR,
      GENERATOR_OIS_BRL.isEndOfMonth());
  private static final ZonedDateTime SETTLE_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, GENERATOR_OIS_BRL.getSpotLag(), CALENDAR);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final double NOTIONAL = 123456789.0;
  private static final double RATE = 0.02;
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP_DEFINITION_REC = SwapFixedCompoundedONCompoundedDefinition
      .from(SETTLE_DATE, SWAP_TENOR, NOTIONAL, GENERATOR_OIS_BRL, RATE, false);
  private static final SwaptionCashFixedCompoundedONCompoundingDefinition SWAPTION_DEFINITION_LONG_REC =
      SwaptionCashFixedCompoundedONCompoundingDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_REC, true);
  private static final SwaptionCashFixedCompoundedONCompounded SWAPTION_LONG_REC = SWAPTION_DEFINITION_LONG_REC.toDerivative(REFERENCE_DATE, CURVES_NAME[0], CURVES_NAME[0]);

  private static final SwaptionCashFixedCompoundedONCompoundedBlackMethod METHOD_BLACK = SwaptionCashFixedCompoundedONCompoundedBlackMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test
  public void presentValue() {
  }

}
