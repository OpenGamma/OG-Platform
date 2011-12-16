/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.interestrate.HullWhiteTestsDataSet;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtils;

/**
 * Test the Bermuda swaption pricing in the Hull-White one factor model.
 */
public class SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethodTest {
  // General
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 22);
  // Total swap - 5Y semi bond vs quarterly money
  private static final Period FORWARD_TENOR = Period.ofYears(1);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, FORWARD_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final Period SWAP_TENOR = Period.ofYears(5);
  private static final double NOTIONAL = 123000000;
  private static final boolean FIXED_IS_PAYER = true;
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, CALENDAR, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR);
  private static final double RATE = 0.0400;
  private static final SwapFixedIborDefinition TOTAL_SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  // Semi-annual expiry
  private static final boolean IS_LONG = true;
  private static final int NB_EXPIRY = TOTAL_SWAP_DEFINITION.getFixedLeg().getNumberOfPayments();
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXPIRY];
  private static final SwapFixedIborDefinition[] EXPIRY_SWAP_DEFINITION = new SwapFixedIborDefinition[NB_EXPIRY];
  static {
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(TOTAL_SWAP_DEFINITION.getFixedLeg().getNthPayment(loopexp).getAccrualStartDate(), -IBOR_SETTLEMENT_DAYS, CALENDAR);
      EXPIRY_SWAP_DEFINITION[loopexp] = TOTAL_SWAP_DEFINITION.trimStart(EXPIRY_DATE[loopexp]);
    }
  }
  private static final SwaptionBermudaFixedIborDefinition BERMUDA_SWAPTION_DEFINITION = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, IS_LONG, EXPIRY_DATE);
  // to derivatives
  //  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSets.createCurves1();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = HullWhiteTestsDataSet.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod METHOD_BERMUDA = new SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_VANILLA = new SwaptionPhysicalFixedIborHullWhiteMethod();
  //  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  private static final SwaptionBermudaFixedIbor BERMUDA_SWAPTION = BERMUDA_SWAPTION_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  @Test
  /**
   * Test the present value.
   */
  public void presentValue() {
    CurrencyAmount pv = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, BUNDLE_HW);
    // European swaptions 
    SwaptionPhysicalFixedIborDefinition[] swaptionEuropeanDefinition = new SwaptionPhysicalFixedIborDefinition[NB_EXPIRY];
    SwaptionPhysicalFixedIbor[] swaptionEuropean = new SwaptionPhysicalFixedIbor[NB_EXPIRY];
    //    FixedCouponSwap<Coupon>[] swap = new FixedCouponSwap[NB_EXPIRY];
    CurrencyAmount[] pvEuropean = new CurrencyAmount[NB_EXPIRY];
    //    double[] pvSwap = new double[NB_EXPIRY];
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      swaptionEuropeanDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[loopexp], EXPIRY_SWAP_DEFINITION[loopexp], IS_LONG);
      swaptionEuropean[loopexp] = swaptionEuropeanDefinition[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      //      swap[loopexp] = EXPIRY_SWAP_DEFINITION[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvEuropean[loopexp] = METHOD_VANILLA.presentValue(swaptionEuropean[loopexp], BUNDLE_HW);
      //      pvSwap[loopexp] = PVC.visit(swap[loopexp], CURVES);
      assertTrue("Bermuda swaption vs European", pv.getAmount() >= pvEuropean[loopexp].getAmount());
    }
  }

  @Test
  /**
   * Test the present value.
   */
  public void longShortParity() {
    CurrencyAmount pvLong = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, BUNDLE_HW);
    final SwaptionBermudaFixedIborDefinition bermudaShortDefinition = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, !IS_LONG, EXPIRY_DATE);
    final SwaptionBermudaFixedIbor bermudShort = bermudaShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    CurrencyAmount pvShort = METHOD_BERMUDA.presentValue(bermudShort, BUNDLE_HW);
    assertEquals("Bermuda swaption pv: short/long parity", pvLong.getAmount(), -pvShort.getAmount(), 1.0E-2);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 20;
    // Creates different swaptions
    final SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbTest];
    final SwapFixedIborDefinition[][] swapExpiryDefinition = new SwapFixedIborDefinition[nbTest][NB_EXPIRY];
    final SwaptionBermudaFixedIborDefinition[] swaptionBermudaDefinition = new SwaptionBermudaFixedIborDefinition[nbTest];
    final SwaptionBermudaFixedIbor[] swaptionBermuda = new SwaptionBermudaFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      swapDefinition[looptest] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE + looptest * 0.0010 / nbTest, FIXED_IS_PAYER);
      for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
        swapExpiryDefinition[looptest][loopexp] = swapDefinition[looptest].trimStart(EXPIRY_DATE[loopexp]);
      }
      swaptionBermudaDefinition[looptest] = new SwaptionBermudaFixedIborDefinition(swapExpiryDefinition[looptest], IS_LONG, EXPIRY_DATE);
      swaptionBermuda[looptest] = swaptionBermudaDefinition[looptest].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    // Loop for pricing
    CurrencyAmount[] pv = new CurrencyAmount[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_BERMUDA.presentValue(swaptionBermuda[looptest], BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Bermuda swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 27-Jul-11: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 500 ms for 20 swaptions.

    double total = 0.0;
    for (int looptest = 0; looptest < nbTest; looptest++) {
      total += pv[looptest].getAmount();
    }
    assertEquals("Bermuda swaption pv performance", pv[nbTest / 2].getAmount(), total / nbTest, 1.0E+5);
  }
}
