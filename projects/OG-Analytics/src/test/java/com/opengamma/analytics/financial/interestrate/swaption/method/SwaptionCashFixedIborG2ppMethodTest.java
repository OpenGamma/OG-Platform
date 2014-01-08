/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborG2ppMethodTest {
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(6);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SETTLEMENT_DAYS, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PHYS_PAYER_LONG = SWAPTION_PHYS_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Calculator
  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_PHYS_APPROXIMATION = new SwaptionPhysicalFixedIborG2ppApproximationMethod();
  private static final SwaptionCashFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionCashFixedIborG2ppNumericalIntegrationMethod();
  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppPiecewiseConstantDataBundle BUNDLE_G2PP = new G2ppPiecewiseConstantDataBundle(PARAMETERS_G2PP, CURVES);

  //  @Test(enabled = false)
  //  /**
  //   * Test the present value vs a external system. "enabled = false" for the standard testing: the external system is using a TimeCalculator with ACT/365.
  //   */
  //  public void presentValueExternal() {
  //    G2ppPiecewiseConstantParameters parametersCst = G2ppTestsDataSet.createG2ppCstParameters();
  //    final YieldAndDiscountCurve curve5 = new YieldCurve(ConstantDoublesCurve.from(0.05));
  //    final YieldCurveBundle curves = new YieldCurveBundle();
  //    curves.setCurve(FUNDING_CURVE_NAME, curve5);
  //    curves.setCurve(FORWARD_CURVE_NAME, curve5);
  //    G2ppPiecewiseConstantDataBundle bundleCst = new G2ppPiecewiseConstantDataBundle(parametersCst, curves);
  //    CurrencyAmount pv = METHOD_G2PP_APPROXIMATION.presentValue(SWAPTION_PAYER_LONG, bundleCst);
  //    double pvExternal = 6885626.28245924; // ! TimeCalculator with ACT/365
  //    assertEquals("Swaption physical - G2++ - present value - external system", pvExternal, pv.getAmount(), 1E-2);
  //  }

  @Test(enabled = true)
  /**
   * Tests the present value vs a physical delivery swaption.
   */
  public void physical() {
    final CurrencyAmount pvPhys = METHOD_G2PP_PHYS_APPROXIMATION.presentValue(SWAPTION_PHYS_PAYER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvCash = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvPhys.getAmount(), pvCash.getAmount(), 2.0E+5);
  }

  @Test(enabled = true)
  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final double pvExpected = 5121815.309;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final CurrencyAmount pvPayerLong = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvPayerShort = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(), -pvPayerShort.getAmount(), 1E-2);
    final CurrencyAmount pvReceiverLong = METHOD_G2PP_NI.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_G2PP);
    final CurrencyAmount pvReceiverShort = METHOD_G2PP_NI.presentValue(SWAPTION_RECEIVER_SHORT, BUNDLE_G2PP);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(), -pvReceiverShort.getAmount(), 1E-2);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;
    final CurrencyAmount[] pvPayerLongNI = new CurrencyAmount[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI[looptest] = METHOD_G2PP_NI.presentValue(SWAPTION_PAYER_LONG, BUNDLE_G2PP);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption cash G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 13-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 910 ms for 100 swaptions.

    System.out.println("G2++ numerical integration - present value: " + pvPayerLongNI[0]);
  }

}
