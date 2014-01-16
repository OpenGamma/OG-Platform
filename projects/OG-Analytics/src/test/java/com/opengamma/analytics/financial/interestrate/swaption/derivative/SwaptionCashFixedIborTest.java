/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.swaption.derivative;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.instrument.swaption.SwaptionCashFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SwaptionCashFixedIborTest {
  // Swaption description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 3, 28);
  private static final boolean IS_LONG = true;
  // Swap 2Y description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 100000000; //100m
  //  Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_PAYER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY_RECEIVER = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, !FIXED_IS_PAYER);
  //  Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_RECEIVER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER, CALENDAR);
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY_PAYER = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, FIXED_IS_PAYER, CALENDAR);
  // Swaption construction: All combinations
  private static final IndexSwap CMS_INDEX = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, INDEX, ANNUITY_TENOR, CALENDAR);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_PAYER = new SwapFixedIborDefinition(FIXED_ANNUITY_PAYER, IBOR_ANNUITY_RECEIVER);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_RECEIVER = new SwapFixedIborDefinition(FIXED_ANNUITY_RECEIVER, IBOR_ANNUITY_PAYER);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_LONG_PAYER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_PAYER, true, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEFINITION_SHORT_RECEIVER = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP_DEFINITION_RECEIVER, false, !IS_LONG);
  // to derivatives
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 8, 18);
  private static final SwapFixedCoupon<Coupon> SWAP_PAYER = SWAP_DEFINITION_PAYER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_LONG_PAYER = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE);
  private static final SwaptionCashFixedIbor SWAPTION_SHORT_RECEIVER = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE);

  /**
   * Tests the equal and hashCode methods.
   */
  @Test
  public void equalHash() {
    assertTrue(SWAPTION_LONG_PAYER.equals(SWAPTION_LONG_PAYER));
    final SwaptionCashFixedIbor other = SWAPTION_DEFINITION_LONG_PAYER.toDerivative(REFERENCE_DATE);
    assertTrue(SWAPTION_LONG_PAYER.equals(other));
    assertTrue(SWAPTION_LONG_PAYER.hashCode() == other.hashCode());
    assertEquals(SWAPTION_LONG_PAYER.toString(), other.toString());
    final SwaptionCashFixedIbor otherS = SWAPTION_DEFINITION_SHORT_RECEIVER.toDerivative(REFERENCE_DATE);
    assertTrue(SWAPTION_SHORT_RECEIVER.equals(otherS));
    assertTrue(SWAPTION_SHORT_RECEIVER.hashCode() == otherS.hashCode());
    SwaptionCashFixedIbor modifiedSwaption;
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry() - 0.01, SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime() - 0.01, FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, !IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), SWAP_PAYER, SWAPTION_LONG_PAYER.getSettlementTime(), !FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    final SwapFixedIborDefinition otherSwapDefinition = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX, 2 * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    final SwapFixedCoupon<Coupon> otherSwap = otherSwapDefinition.toDerivative(REFERENCE_DATE);
    modifiedSwaption = SwaptionCashFixedIbor.from(SWAPTION_LONG_PAYER.getTimeToExpiry(), otherSwap, SWAPTION_LONG_PAYER.getSettlementTime(), FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION_LONG_PAYER.equals(modifiedSwaption));
    assertFalse(SWAPTION_LONG_PAYER.equals(EXPIRY_DATE));
    assertFalse(SWAPTION_LONG_PAYER.equals(null));
  }

}
