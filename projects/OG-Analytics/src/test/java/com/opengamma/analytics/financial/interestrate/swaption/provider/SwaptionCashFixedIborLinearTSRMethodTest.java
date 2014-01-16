/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.provider;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.option.definition.SABRInterestRateParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.SABRDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRSwaptionProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Class to test the present value of the cash-settled European swaption in the Linear Terminal Swap Rate method.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborLinearTSRMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR6M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[1];
  private static final Currency EUR = EURIBOR6M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final SABRInterestRateParameters SABR_PARAMETER = SABRDataSets.createSABR1();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GeneratorSwapFixedIborMaster.getInstance().getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final SABRSwaptionProviderDiscount SABR_MULTICURVES = new SABRSwaptionProviderDiscount(MULTICURVES, SABR_PARAMETER, EUR1YEURIBOR6M);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);

  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(12);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, EURIBOR6M, SWAP_TENOR, CALENDAR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, EURIBOR6M.getSpotLag(), CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0200;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);
  private static final boolean IS_LONG = true;
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  //to derivatives
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIborLinearTSRMethod METHOD_CASH_TSR = new SwaptionCashFixedIborLinearTSRMethod();
  private static final SwaptionCashFixedIborSABRMethod METHOD_CASH_SABR = SwaptionCashFixedIborSABRMethod.getInstance();
  private static final SwaptionPhysicalFixedIborSABRMethod METHOD_PHYS_SABR = SwaptionPhysicalFixedIborSABRMethod.getInstance();

  @Test(enabled = true)
  /**
   * Tests the present value v hard-coded values.
   */
  public void presentValue() {
    //    double pvSABR = METHOD_CASH_SABR.presentValue(SWAPTION_PAYER_LONG, SABR_BUNDLE);
    final MultipleCurrencyAmount pvPayerTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_LONG, SABR_MULTICURVES);
    final double pvPayerExpected = 1917641.961;
    assertEquals("Cash-settled swaption: linear TSR: present value", pvPayerExpected, pvPayerTSR.getAmount(EUR), 1E+0);
    //    double pvSABR = METHOD_CASH_SABR.presentValue(SWAPTION_RECEIVER_LONG, SABR_BUNDLE);
    final MultipleCurrencyAmount pvReceiverTSR = METHOD_CASH_TSR.presentValue(SWAPTION_RECEIVER_LONG, SABR_MULTICURVES);
    final double pvReceiverExpected = 4102844.469;
    assertEquals("Cash-settled swaption: linear TSR: present value", pvReceiverExpected, pvReceiverTSR.getAmount(EUR), 1E+0);
  }

  @Test(enabled = true)
  public void presentValueLongShortParity() {
    final MultipleCurrencyAmount pvLongTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_LONG, SABR_MULTICURVES);
    final MultipleCurrencyAmount pvShortTSR = METHOD_CASH_TSR.presentValue(SWAPTION_PAYER_SHORT, SABR_MULTICURVES);
    assertEquals("Cash-settled swaption: linear TSR: present value - long/short parity", -pvLongTSR.getAmount(EUR), pvShortTSR.getAmount(EUR), 1E-2);
  }

  @Test(enabled = true)
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
      swaptionCash[loopstrike] = swaptionCashDefinition[loopstrike].toDerivative(REFERENCE_DATE);
      swaptionPhysDefinition[loopstrike] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[loopstrike], true, IS_LONG);
      swaptionPhys[loopstrike] = swaptionPhysDefinition[loopstrike].toDerivative(REFERENCE_DATE);
    }
    final double[] pvCashStandard = new double[nbStrike + 1];
    final double[] pvCashTSR = new double[nbStrike + 1];
    final double[] pvPhysical = new double[nbStrike + 1];
    for (int loopstrike = 0; loopstrike < nbStrike + 1; loopstrike++) {
      pvCashStandard[loopstrike] = METHOD_CASH_SABR.presentValue(swaptionCash[loopstrike], SABR_MULTICURVES).getAmount(EUR);
      pvCashTSR[loopstrike] = METHOD_CASH_TSR.presentValue(swaptionCash[loopstrike], SABR_MULTICURVES).getAmount(EUR);
      pvPhysical[loopstrike] = METHOD_PHYS_SABR.presentValue(swaptionPhys[loopstrike], SABR_MULTICURVES).getAmount(EUR);
    }
  }

}
