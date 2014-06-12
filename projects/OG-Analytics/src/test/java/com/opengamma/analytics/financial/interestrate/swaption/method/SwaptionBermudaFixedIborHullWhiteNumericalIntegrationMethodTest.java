/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.method;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionBermudaFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionPhysicalFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.TestsDataSetsSABR;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionPhysicalFixedIbor;
import com.opengamma.analytics.financial.model.interestrate.TestsDataSetHullWhite;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
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
 * Test the Bermuda swaption pricing in the Hull-White one factor model.
 * @deprecated This class tests deprecated functionality.
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethodTest {
  // General
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 22);
  // Total swap - 5Y semi bond vs quarterly money
  private static final Period FORWARD_TENOR = Period.ofYears(1);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, FORWARD_TENOR, BUSINESS_DAY, CALENDAR, IS_EOM);
  private static final Period SWAP_TENOR = Period.ofYears(10);  // <<<<======================
  private static final double NOTIONAL = 123000000;
  private static final boolean FIXED_IS_PAYER = true;
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6); // <<<<======================
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final int IBOR_SETTLEMENT_DAYS = 2;
  private static final DayCount IBOR_DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SETTLEMENT_DAYS, IBOR_DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, SWAP_TENOR, CALENDAR);
  private static final double RATE = 0.0400;
  private static final SwapFixedIborDefinition TOTAL_SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
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
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_NAME = "Forward";
  private static final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
  private static final YieldCurveBundle CURVES = TestsDataSetsSABR.createCurves1();
  private static final HullWhiteOneFactorPiecewiseConstantParameters PARAMETERS_HW = TestsDataSetHullWhite.createHullWhiteParameters();
  private static final HullWhiteOneFactorPiecewiseConstantDataBundle BUNDLE_HW = new HullWhiteOneFactorPiecewiseConstantDataBundle(PARAMETERS_HW, CURVES);
  private static final SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod METHOD_BERMUDA = SwaptionBermudaFixedIborHullWhiteNumericalIntegrationMethod.getInstance();
  private static final SwaptionPhysicalFixedIborHullWhiteMethod METHOD_VANILLA = new SwaptionPhysicalFixedIborHullWhiteMethod();

  private static final SwaptionBermudaFixedIbor BERMUDA_SWAPTION = BERMUDA_SWAPTION_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);

  private static final double TOLERANCE_PRICE = 1.0E+0;

  @Test
  /**
   * Test the present value against European swaptions.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, BUNDLE_HW);
    final double pvPrevious = 8215343.371671409; // Hard-coded - previous run
    assertEquals("Bermuda swaption vs European", pvPrevious, pv.getAmount(), TOLERANCE_PRICE);
    // European swaptions
    final SwaptionPhysicalFixedIborDefinition[] swaptionEuropeanDefinition = new SwaptionPhysicalFixedIborDefinition[NB_EXPIRY];
    final SwaptionPhysicalFixedIbor[] swaptionEuropean = new SwaptionPhysicalFixedIbor[NB_EXPIRY];
    final CurrencyAmount[] pvEuropean = new CurrencyAmount[NB_EXPIRY];
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      swaptionEuropeanDefinition[loopexp] = SwaptionPhysicalFixedIborDefinition.from(EXPIRY_DATE[loopexp], EXPIRY_SWAP_DEFINITION[loopexp], FIXED_IS_PAYER, IS_LONG);
      swaptionEuropean[loopexp] = swaptionEuropeanDefinition[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      pvEuropean[loopexp] = METHOD_VANILLA.presentValue(swaptionEuropean[loopexp], BUNDLE_HW);
      assertTrue("Bermuda swaption vs European", pv.getAmount() >= pvEuropean[loopexp].getAmount());
    }
  }

  //TODO: test present value with external values

  @Test
  /**
   * Test the present value long/short parity.
   */
  public void longShortParity() {
    final CurrencyAmount pvLong = METHOD_BERMUDA.presentValue(BERMUDA_SWAPTION, BUNDLE_HW);
    final SwaptionBermudaFixedIborDefinition bermudaShortDefinition = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, !IS_LONG, EXPIRY_DATE);
    final SwaptionBermudaFixedIbor bermudShort = bermudaShortDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);
    final CurrencyAmount pvShort = METHOD_BERMUDA.presentValue(bermudShort, BUNDLE_HW);
    assertEquals("Bermuda swaption pv: short/long parity", pvLong.getAmount(), -pvShort.getAmount(), 1.0E-2);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performance() {
    long startTime, endTime;
    final int nbTest = 100000;
    // Creates different swaptions
    final SwapFixedIborDefinition[] swapDefinition = new SwapFixedIborDefinition[nbTest];
    final SwapFixedIborDefinition[][] swapExpiryDefinition = new SwapFixedIborDefinition[nbTest][NB_EXPIRY];
    final SwaptionBermudaFixedIborDefinition[] swaptionBermudaDefinition = new SwaptionBermudaFixedIborDefinition[nbTest];
    final SwaptionBermudaFixedIbor[] swaptionBermuda = new SwaptionBermudaFixedIbor[nbTest];
    for (int looptest = 0; looptest < nbTest; looptest++) {
      swapDefinition[looptest] = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE + looptest * 0.0010 / nbTest, FIXED_IS_PAYER, CALENDAR);
      for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
        swapExpiryDefinition[looptest][loopexp] = swapDefinition[looptest].trimStart(EXPIRY_DATE[loopexp]);
      }
      swaptionBermudaDefinition[looptest] = new SwaptionBermudaFixedIborDefinition(swapExpiryDefinition[looptest], IS_LONG, EXPIRY_DATE);
      swaptionBermuda[looptest] = swaptionBermudaDefinition[looptest].toDerivative(REFERENCE_DATE, CURVES_NAME);
    }
    // Loop for pricing
    final CurrencyAmount[] pv = new CurrencyAmount[nbTest];
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      pv[looptest] = METHOD_BERMUDA.presentValue(swaptionBermuda[looptest], BUNDLE_HW);
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Bermuda swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 19-Jan-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 440 ms for 20 swaptions.

    double total = 0.0;
    for (int looptest = 0; looptest < nbTest; looptest++) {
      total += pv[looptest].getAmount();
    }
    assertEquals("Bermuda swaption pv performance", pv[nbTest / 2].getAmount(), total / nbTest, 1.0E+5);
  }

  @Test(enabled = false)
  /**
   * Tests of performance. "enabled = false" for the standard testing.
   */
  public void performanceLessStorage() {
    long startTime, endTime;
    final int nbTest = 10000;
    CurrencyAmount totalPv = CurrencyAmount.of(CUR, 0.0);

    // Creates swaption
    final SwapFixedIborDefinition swapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedIborDefinition[] swapExpiryDefinition = new SwapFixedIborDefinition[NB_EXPIRY];
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      swapExpiryDefinition[loopexp] = swapDefinition.trimStart(EXPIRY_DATE[loopexp]);
    }
    final SwaptionBermudaFixedIborDefinition swaptionBermudaDefinition = new SwaptionBermudaFixedIborDefinition(swapExpiryDefinition, IS_LONG, EXPIRY_DATE);
    final SwaptionBermudaFixedIbor swaptionBermuda = swaptionBermudaDefinition.toDerivative(REFERENCE_DATE, CURVES_NAME);

    // Loop for pricing
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      totalPv = totalPv.plus(METHOD_BERMUDA.presentValue(swaptionBermuda, BUNDLE_HW));
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Bermuda swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");
    // Performance note: HW price: 19-Jan-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 440 ms for 20 swaptions.

    // 5Y Bermudan Swaption with 6m looks (Hull-White Gaussian Integration) = 10 expiries ~ 3.7ms/price
    //10000 pv Bermuda swaption Hull-White numerical integration method: 37545 ms
    //100000 pv Bermuda swaption Hull-White numerical integration method: 363339 ms
    //1000000 pv Bermuda swaption Hull-White numerical integration method: 3720939 ms

    //30Y Bermudan Swaption with 6m looks (Hull-White Gaussian Integration) = 60 expiries ~ 87ms/price
    //10000 pv Bermuda swaption Hull-White numerical integration method: 867701 ms

    final CurrencyAmount singlePV = METHOD_BERMUDA.presentValue(swaptionBermuda, BUNDLE_HW);
    assertEquals("Bermuda swaption pv performance", totalPv.getAmount() / nbTest, singlePV.getAmount(), 1.0E+5);

    // Loop for pricing
    startTime = System.currentTimeMillis();
    for (int looptest = 0; looptest < nbTest; looptest++) {
      totalPv = totalPv.plus(METHOD_BERMUDA.presentValue(swaptionBermuda, BUNDLE_HW));
    }
    endTime = System.currentTimeMillis();
    System.out.println(nbTest + " pv Bermuda swaption Hull-White numerical integration method: " + (endTime - startTime) + " ms");

  }

}
