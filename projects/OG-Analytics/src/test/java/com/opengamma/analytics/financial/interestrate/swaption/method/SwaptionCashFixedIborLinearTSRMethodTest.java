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
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
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
 * Class to test the present value of the cash-settled European swaption in the Linear Terminal Swap Rate method.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborLinearTSRMethodTest {
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
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final SABRInterestRateParameters SABR_PARAMETER = TestsDataSetsSABR.createSABR1();
  private static final SABRInterestRateDataBundle SABR_BUNDLE = new SABRInterestRateDataBundle(SABR_PARAMETER, CURVES);
  //  private static final FixedCouponSwap<Coupon> SWAP_PAYER = SWAP_PAYER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionCashFixedIborLinearTSRMethod METHOD_CASH_TSR = new SwaptionCashFixedIborLinearTSRMethod();
  private static final SwaptionCashFixedIborSABRMethod METHOD_CASH_SABR = SwaptionCashFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_PHYS_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

  @Test(enabled = true)
  /**
   * Tests the present value v hard-coded values.
   */
  public void presentValue() {
    //    double pvSABR = METHOD_CASH_SABR.presentValue(SWAPTION_PAYER_LONG, SABR_BUNDLE);
    final CurrencyAmount pvPayerTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_LONG, SABR_BUNDLE);
    final double pvPayerExpected = 5195841.44;
    assertEquals("Cash-settled swaption: linear TSR: present value", pvPayerExpected, pvPayerTSR.getAmount(), 1E+0);
    assertEquals("Cash-settled swaption: linear TSR: present value", CUR, pvPayerTSR.getCurrency());
    //    double pvSABR = METHOD_CASH_SABR.presentValue(SWAPTION_RECEIVER_LONG, SABR_BUNDLE);
    final CurrencyAmount pvReceiverTSR = METHOD_CASH_TSR.presentValue(SWAPTION_RECEIVER_LONG, SABR_BUNDLE);
    final double pvReceiverExpected = 2242621.75;
    assertEquals("Cash-settled swaption: linear TSR: present value", pvReceiverExpected, pvReceiverTSR.getAmount(), 1E+0);
  }

  @Test(enabled = true)
  public void presentValueLongShortParity() {
    final CurrencyAmount pvLongTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_LONG, SABR_BUNDLE);
    final CurrencyAmount pvShortTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_SHORT, SABR_BUNDLE);
    assertEquals("Cash-settled swaption: linear TSR: present value - long/short parity", -pvLongTSR.getAmount(), pvShortTSR.getAmount(), 1E-2);
  }

  @Test(enabled = false)
  public void presentValueMultiStrike() {
    final int nbStrike = 10;
    final double strikeMin = 0.030;
    final double strikeMax = 0.050;
    final double[] strike = new double[nbStrike + 1];
    final SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbStrike + 1];
    final SwaptionCashFixedIborDefinition[] swaptionCashDefinition = new SwaptionCashFixedIborDefinition[nbStrike + 1];
    final SwaptionCashFixedIbor[] swaptionCash = new SwaptionCashFixedIbor[nbStrike + 1];
    final SwaptionPhysicalFixedIborDefinition[] swaptionPhysDefinition = new SwaptionPhysicalFixedIborDefinition[nbStrike + 1];
    final SwaptionPhysicalFixedIbor[] swaptionPhys = new SwaptionPhysicalFixedIbor[nbStrike + 1];
    for (int loopstrike = 0; loopstrike < nbStrike + 1; loopstrike++) {
      strike[loopstrike] = strikeMin + loopstrike * (strikeMax - strikeMin) / nbStrike;
      swapDefinition[loopstrike] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, strike[loopstrike], FIXED_IS_PAYER, CALENDAR);
      swaptionCashDefinition[loopstrike] = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[loopstrike], true, IS_LONG);
      swaptionCash[loopstrike] = swaptionCashDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
      swaptionPhysDefinition[loopstrike] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[loopstrike], true, IS_LONG);
      swaptionPhys[loopstrike] = swaptionPhysDefinition[loopstrike].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    final double[] pvCashStandard = new double[nbStrike + 1];
    final double[] pvCashTSR = new double[nbStrike + 1];
    final double[] pvPhysical = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike < nbStrike + 1; loopstrike++) {
      pvCashStandard[loopstrike] = METHOD_CASH_SABR.presentValue(swaptionCash[loopstrike], SABR_BUNDLE).getAmount();
      pvCashTSR[loopstrike] = METHOD_CASH_TSR.presentValue(swaptionCash[loopstrike], SABR_BUNDLE).getAmount();
      pvPhysical[loopstrike] = METHOD_PHYS_SABR.presentValue(swaptionPhys[loopstrike], SABR_BUNDLE).getAmount();
    }
  }

}
