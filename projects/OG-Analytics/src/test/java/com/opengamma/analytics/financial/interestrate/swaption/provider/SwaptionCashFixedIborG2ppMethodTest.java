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
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetG2pp;
import com.opengamma.analytics.financial.model.interestrate.definition.G2ppPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderDiscountDataSets;
import com.opengamma.analytics.financial.provider.description.interestrate.G2ppProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in G2++ model.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborG2ppMethodTest {

  private static final MulticurveProviderDiscount MULTICURVES = MulticurveProviderDiscountDataSets.createMulticurveEurUsd();
  private static final IborIndex EURIBOR3M = MulticurveProviderDiscountDataSets.getIndexesIborMulticurveEurUsd()[0];
  private static final Currency EUR = EURIBOR3M.getCurrency();
  private static final Calendar CALENDAR = MulticurveProviderDiscountDataSets.getEURCalendar();

  private static final G2ppPiecewiseConstantParameters PARAMETERS_G2PP = TestsDataSetG2pp.createG2ppParameters1();
  private static final G2ppProviderDiscount G2PP_MULTICURVES = new G2ppProviderDiscount(MULTICURVES, PARAMETERS_G2PP, EUR);

  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final int SPOT_LAG = EURIBOR3M.getSpotLag();
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR6M", CALENDAR);
  private static final IndexSwap CMS_INDEX = new IndexSwap(EUR1YEURIBOR6M, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final boolean IS_LONG = true;
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, SPOT_LAG, CALENDAR);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER, CALENDAR);

  // Swaption 5Yx5Y
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, true, !IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, false, !IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PHYS_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, FIXED_IS_PAYER, IS_LONG);
  //to derivatives

  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_RECEIVER = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_PAYER = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PHYS_PAYER_LONG = SWAPTION_PHYS_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE);

  private static final SwaptionPhysicalFixedIborG2ppApproximationMethod METHOD_G2PP_PHYS_APPROXIMATION = SwaptionPhysicalFixedIborG2ppApproximationMethod.getInstance();
  private static final SwaptionCashFixedIborG2ppNumericalIntegrationMethod METHOD_G2PP_NI = new SwaptionCashFixedIborG2ppNumericalIntegrationMethod();

  private static final double TOLERANCE_PV = 1.0E-2;

  @Test(enabled = true)
  /**
   * Tests the present value vs a physical delivery swaption.
   */
  public void physical() {
    final MultipleCurrencyAmount pvPhys = METHOD_G2PP_PHYS_APPROXIMATION.presentValue(SWAPTION_PHYS_PAYER_LONG, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvCash = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvPhys.getAmount(EUR), pvCash.getAmount(EUR), 2.0E+5);
  }

  @Test(enabled = true)
  /**
   * Test the present value vs a hard-coded value.
   */
  public void presentValue() {
    final MultipleCurrencyAmount pv = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final double pvExpected = 1583688.804;
    assertEquals("Swaption physical - G2++ - present value - hard coded value", pvExpected, pv.getAmount(EUR), 1E-2);
  }

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    final MultipleCurrencyAmount pvPayerLong = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvPayerShort = METHOD_G2PP_NI.presentValue(SWAPTION_SHORT_PAYER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvPayerLong.getAmount(EUR), -pvPayerShort.getAmount(EUR), TOLERANCE_PV);
    final MultipleCurrencyAmount pvReceiverLong = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_RECEIVER, G2PP_MULTICURVES);
    final MultipleCurrencyAmount pvReceiverShort = METHOD_G2PP_NI.presentValue(SWAPTION_SHORT_RECEIVER, G2PP_MULTICURVES);
    assertEquals("Swaption physical - G2++ - present value - long/short parity", pvReceiverLong.getAmount(EUR), -pvReceiverShort.getAmount(EUR), TOLERANCE_PV);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100;

    final MultipleCurrencyAmount[] pvPayerLongNI = new MultipleCurrencyAmount[nbTest];

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongNI[looptest] = METHOD_G2PP_NI.presentValue(SWAPTION_LONG_PAYER, G2PP_MULTICURVES);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv swaption cash G2++ numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: G2++ price: 20-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 650 ms for 100 swaptions.

    System.out.println("G2++ numerical integration - present value: " + pvPayerLongNI[0]);
  }

}
