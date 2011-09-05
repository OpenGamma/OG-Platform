/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.LiborMarketModelDisplacedDiffusionTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionDataBundle;
import com.opengamma.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the pricing of physical delivery swaption in LMM displaced diffusion.
 */
public class SwaptionPhysicalFixedIborLMMDDMethodTest {
  // Swaption 5Yx5Y
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final int SETTLEMENT_DAYS = 2;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final int SWAP_TENOR_YEAR = 5;
  private static final Period SWAP_TENOR = Period.ofYears(SWAP_TENOR_YEAR);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final CMSIndex CMS_INDEX = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2016, 7, 7);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(EXPIRY_DATE, CALENDAR, SETTLEMENT_DAYS);
  private static final double NOTIONAL = 100000000; //100m
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final SwapFixedIborDefinition SWAP_PAYER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final SwapFixedIborDefinition SWAP_RECEIVER_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, !FIXED_IS_PAYER);
  private static final boolean IS_LONG = true;
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_LONG_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, IS_LONG);
  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_PAYER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_PAYER_DEFINITION, !IS_LONG);
  //  private static final SwaptionPhysicalFixedIborDefinition SWAPTION_RECEIVER_SHORT_DEFINITION = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, SWAP_RECEIVER_DEFINITION, !IS_LONG);
  //to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 7);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();

  private static final FixedCouponSwap<Coupon> SWAP_RECEIVER = SWAP_RECEIVER_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_LONG = SWAPTION_PAYER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_LONG = SWAPTION_RECEIVER_LONG_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final SwaptionPhysicalFixedIbor SWAPTION_PAYER_SHORT = SWAPTION_PAYER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  //  private static final SwaptionPhysicalFixedIbor SWAPTION_RECEIVER_SHORT = SWAPTION_RECEIVER_SHORT_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  // Parameters and methods
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final SwaptionPhysicalFixedIborLMMDDMethod METHOD_LMM = new SwaptionPhysicalFixedIborLMMDDMethod();
  private static final LiborMarketModelDisplacedDiffusionParameters PARAMETERS_LMM = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION);
  private static final LiborMarketModelDisplacedDiffusionDataBundle BUNDLE_LMM = new LiborMarketModelDisplacedDiffusionDataBundle(PARAMETERS_LMM, CURVES);

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    double pvPreviousRun = 2860520.342;
    CurrencyAmount pv = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value", pvPreviousRun, pv.getAmount(), 1E-2);
  }

  // TODO: Present value check with external values and MC.

  @Test
  /**
   * Tests long/short parity.
   */
  public void longShortParity() {
    CurrencyAmount pvLong = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, BUNDLE_LMM);
    CurrencyAmount pvShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    assertEquals("Swaption physical - LMM - present value - long/short parity", pvLong.getAmount(), -pvShort.getAmount(), 1E-2);
  }

  @Test
  /**
   * Tests payer/receiver/swap parity.
   */
  public void payerReceiverParity() {
    CurrencyAmount pvReceiverLong = METHOD_LMM.presentValue(SWAPTION_RECEIVER_LONG, BUNDLE_LMM);
    CurrencyAmount pvPayerShort = METHOD_LMM.presentValue(SWAPTION_PAYER_SHORT, BUNDLE_LMM);
    double pvSwap = PVC.visit(SWAP_RECEIVER, CURVES);
    assertEquals("Swaption physical - LMM - present value - payer/receiver/swap parity", pvReceiverLong.getAmount() + pvPayerShort.getAmount(), pvSwap, 1E-2);
  }

  @Test
  /**
   * Test the present value LMM volatility parameters sensitivity.
   */
  public void presentValueLMMSensitivity() {
    double[][] pvLmmSensi = METHOD_LMM.presentValueLMMSensitivity(SWAPTION_PAYER_LONG, BUNDLE_LMM);

    double shift = 1.0E-6;
    LiborMarketModelDisplacedDiffusionParameters parameterShiftPlus = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION, shift);
    LiborMarketModelDisplacedDiffusionDataBundle bundleShiftPlus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftPlus, CURVES);
    CurrencyAmount pvShiftPlus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftPlus);
    LiborMarketModelDisplacedDiffusionParameters parameterShiftMinus = LiborMarketModelDisplacedDiffusionTestsDataSet.createLMMParameters(REFERENCE_DATE, SWAP_PAYER_DEFINITION, -shift);
    LiborMarketModelDisplacedDiffusionDataBundle bundleShiftMinus = new LiborMarketModelDisplacedDiffusionDataBundle(parameterShiftMinus, CURVES);
    CurrencyAmount pvShiftMinus = METHOD_LMM.presentValue(SWAPTION_PAYER_LONG, bundleShiftMinus);
    double pvLmmSensiTotExpected = (pvShiftPlus.getAmount() - pvShiftMinus.getAmount()) / (2 * shift);
    double pvLmmSensiTot = 0.0;
    for (int loopperiod = 0; loopperiod < pvLmmSensi.length; loopperiod++) {
      for (int loopfact = 0; loopfact < pvLmmSensi[0].length; loopfact++) {
        pvLmmSensiTot += pvLmmSensi[loopperiod][loopfact];
      }
    }
    assertEquals("Swaption physical - LMM - present value Lmm parameters sensitivity", pvLmmSensiTotExpected, pvLmmSensiTot, 1E+4);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 1000;

    double startRate = 0.03;
    double[] rate = new double[nbTest];
    SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbTest];
    SwaptionPhysicalFixedIborDefinition[] swaptionDefinition = new SwaptionPhysicalFixedIborDefinition[nbTest];
    SwaptionPhysicalFixedIbor[] swaption = new SwaptionPhysicalFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      rate[looptest] = startRate + 0.00001 * looptest;
      swapDefinition[looptest] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, rate[looptest], FIXED_IS_PAYER);
      swaptionDefinition[looptest] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE, swapDefinition[looptest], IS_LONG);
      swaption[looptest] = swaptionDefinition[looptest].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }

    CurrencyAmount[] pvPayerLongApproximation = new CurrencyAmount[nbTest];
    double[][][] pvLmmSensi = new double[nbTest][][];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvPayerLongApproximation[looptest] = METHOD_LMM.presentValue(swaption[looptest], BUNDLE_LMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption LMM approximation method: " + (endTime - startTime) + " ms");
    // Performance note: LMM approximation: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 1000 swaptions.

    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pvLmmSensi[looptest] = METHOD_LMM.presentValueLMMSensitivity(swaption[looptest], BUNDLE_LMM);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " swaption LMM approximation method - LMM volatility parameters sensitivity (20x2): " + (endTime - startTime) + " ms");
    // Performance note: LMM approximation - LMM sensi: 1-Sep-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 30 ms for 1000 swaptions.

    System.out.println("Approximation: " + pvPayerLongApproximation.toString());
  }

}
