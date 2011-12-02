/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
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
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.derivative.SwaptionBermudaFixedIbor;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;

/**
 * Tests related to the construction of European physical delivery swaptions and its conversion to derivatives.
 */
public class SwaptionBermudaFixedIborDefinitionTest {
  // General
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2011, 7, 22);
  // Total swap -5Y semi bond vs quarterly money
  private static final Period FORWARD_TENOR = Period.ofYears(1);
  private static final ZonedDateTime SETTLEMENT_DATE = ScheduleCalculator.getAdjustedDate(REFERENCE_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, FORWARD_TENOR);
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
  private static final double RATE = 0.0325;
  private static final SwapFixedIborDefinition TOTAL_SWAP_DEFINITION = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, NOTIONAL, RATE, FIXED_IS_PAYER);
  // Semi-annual expiry
  private static final boolean IS_LONG = true;
  private static final int NB_EXPIRY = TOTAL_SWAP_DEFINITION.getFixedLeg().getNumberOfPayments();
  private static final ZonedDateTime[] EXPIRY_DATE = new ZonedDateTime[NB_EXPIRY];
  private static final SwapFixedIborDefinition[] EXPIRY_SWAP_DEFINITION = new SwapFixedIborDefinition[NB_EXPIRY];
  static {
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      EXPIRY_DATE[loopexp] = ScheduleCalculator.getAdjustedDate(TOTAL_SWAP_DEFINITION.getFixedLeg().getNthPayment(loopexp).getAccrualStartDate(), CALENDAR, -IBOR_SETTLEMENT_DAYS);
      EXPIRY_SWAP_DEFINITION[loopexp] = TOTAL_SWAP_DEFINITION.trimStart(EXPIRY_DATE[loopexp]);
    }
  }
  private static final SwaptionBermudaFixedIborDefinition BERMUDA_SWAPTION_DEFINITION = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, IS_LONG, EXPIRY_DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap() {
    new SwaptionBermudaFixedIborDefinition(null, IS_LONG, EXPIRY_DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, IS_LONG, null);
  }

  @Test
  /**
   * Tests the Bermuda swaption getters.
   */
  public void getter() {
    assertEquals("Getter: underlying swaps", EXPIRY_SWAP_DEFINITION, BERMUDA_SWAPTION_DEFINITION.getUnderlyingSwap());
    assertEquals("Getter: long/short", IS_LONG, BERMUDA_SWAPTION_DEFINITION.isLong());
    assertEquals("Getter: expiry dates", EXPIRY_DATE, BERMUDA_SWAPTION_DEFINITION.getExpiryDate());
  }

  @Test
  /**
   * Tests the Bermuda swaption builder from a unique total swap.
   */
  public void from() {
    SwaptionBermudaFixedIborDefinition bermuda2 = SwaptionBermudaFixedIborDefinition.from(TOTAL_SWAP_DEFINITION, IS_LONG, EXPIRY_DATE);
    assertEquals("Bermuda swaption builder", BERMUDA_SWAPTION_DEFINITION, bermuda2);
  }

  @Test
  /**
   * Tests the equal and hash-code methods.
   */
  public void hashEqual() {
    SwaptionBermudaFixedIborDefinition bermuda2 = SwaptionBermudaFixedIborDefinition.from(TOTAL_SWAP_DEFINITION, IS_LONG, EXPIRY_DATE);
    assertTrue("Bermuda swaption", BERMUDA_SWAPTION_DEFINITION.equals(bermuda2));
    assertEquals("Bermuda swaption", BERMUDA_SWAPTION_DEFINITION.hashCode(), bermuda2.hashCode());
    SwaptionBermudaFixedIborDefinition modified;
    modified = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, !IS_LONG, EXPIRY_DATE);
    assertFalse("Bermuda swaption", BERMUDA_SWAPTION_DEFINITION.equals(modified));
    ZonedDateTime[] expiry2 = new ZonedDateTime[NB_EXPIRY];
    System.arraycopy(EXPIRY_DATE, 0, expiry2, 0, NB_EXPIRY);
    expiry2[0] = EXPIRY_DATE[0].minusDays(1);
    modified = new SwaptionBermudaFixedIborDefinition(EXPIRY_SWAP_DEFINITION, IS_LONG, expiry2);
    assertFalse("Bermuda swaption", BERMUDA_SWAPTION_DEFINITION.equals(modified));
  }

  @Test
  /**
   * Tests the toDerivative method.
   */
  public void toDerivatives() {
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String FUNDING_CURVE_NAME = "Funding";
    final String FORWARD_CURVE_NAME = "Forward";
    final String[] CURVES_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_NAME};
    final double[] expiryTime = new double[NB_EXPIRY];
    final double[] settleTime = new double[NB_EXPIRY];
    @SuppressWarnings("unchecked")
    final FixedCouponSwap<Coupon>[] underlyingSwap = new FixedCouponSwap[NB_EXPIRY];
    for (int loopexp = 0; loopexp < NB_EXPIRY; loopexp++) {
      expiryTime[loopexp] = actAct.getDayCountFraction(REFERENCE_DATE, EXPIRY_DATE[loopexp]);
      underlyingSwap[loopexp] = EXPIRY_SWAP_DEFINITION[loopexp].toDerivative(REFERENCE_DATE, CURVES_NAME);
      settleTime[loopexp] = actAct.getDayCountFraction(REFERENCE_DATE, EXPIRY_SWAP_DEFINITION[loopexp].getFixedLeg().getNthPayment(0).getAccrualStartDate());
    }
    final SwaptionBermudaFixedIbor swaptionBermuda = new SwaptionBermudaFixedIbor(underlyingSwap, IS_LONG, expiryTime, settleTime);
    assertEquals("Swaption Bermuda: to derivatives", swaptionBermuda, BERMUDA_SWAPTION_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME));
  }

}
