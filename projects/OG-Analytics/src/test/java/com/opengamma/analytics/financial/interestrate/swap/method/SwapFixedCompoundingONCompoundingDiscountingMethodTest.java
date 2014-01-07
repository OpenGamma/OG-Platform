/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swap.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsBlack;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.model.option.parameters.BlackFlatSwaptionParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwapFixedCompoundingONCompoundingDiscountingMethodTest {

  // Data
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2013, 9, 25);

  private static final YieldCurveBundle CURVES = TestsDataSetsBlack.createCurvesBRL();
  private static final BlackFlatSwaptionParameters BLACK = TestsDataSetsBlack.createBlackSwaptionBRL();
  //  private static final YieldCurveWithBlackSwaptionBundle CURVES_BLACK = new YieldCurveWithBlackSwaptionBundle(BLACK, CURVES);
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
  private static final SwapFixedCompoundedONCompoundedDefinition SWAP_REC_DEFINITION = SwapFixedCompoundedONCompoundedDefinition
      .from(SETTLE_DATE, SWAP_TENOR, NOTIONAL, GENERATOR_OIS_BRL, RATE, false);
  private static final Swap<CouponFixedAccruedCompounding, CouponONCompounded> SWAP_REC =
      (Swap<CouponFixedAccruedCompounding, CouponONCompounded>) SWAP_REC_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME[0], CURVES_NAME[0]);

  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final SwapFixedCompoundingONCompoundingDiscountingMethod METHOD_SWAP = SwapFixedCompoundingONCompoundingDiscountingMethod.getInstance();

  private static final double TOLERANCE_PV = 1.0E-2;
  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test
  public void forward() {
    final double forward = METHOD_SWAP.forward(SWAP_REC, CURVES);
    final SwapFixedCompoundedONCompoundedDefinition swap0Definition = SwapFixedCompoundedONCompoundedDefinition
        .from(SETTLE_DATE, SWAP_TENOR, NOTIONAL, GENERATOR_OIS_BRL, forward, false);
    final double pv0 = swap0Definition.toDerivative(REFERENCE_DATE, CURVES_NAME[0], CURVES_NAME[0]).accept(PVC, CURVES);
    assertEquals("SwapFixedCompoundingONCompoundingDiscountingMethod: forward", 0.0, pv0, TOLERANCE_PV);
  }

  @Test
  public void forwardModified() {
    final double forwardModified = METHOD_SWAP.forwardModified(SWAP_REC, CURVES);
    final double forward = METHOD_SWAP.forward(SWAP_REC, CURVES);
    final double forwardModifiedExpected = Math.pow(1.0d + forward, SWAP_REC.getFirstLeg().getNthPayment(0).getPaymentYearFraction()) - 1.0d;
    assertEquals("SwapFixedCompoundingONCompoundingDiscountingMethod: forward", forwardModifiedExpected, forwardModified, TOLERANCE_RATE);
  }

}
