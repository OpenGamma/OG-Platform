/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swaption;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexSwap;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.interestrate.swaption.derivative.SwaptionCashFixedIbor;
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
public class SwaptionCashFixedIborDefinitionTest {

  // Swaption: description
  private static final ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 3, 28);
  private static final boolean IS_LONG = true;
  private static final boolean IS_CALL = true;
  // Swap 2Y: description
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 3, 30);
  private static final double NOTIONAL = 1000000; //1m
  // Fixed leg: Semi-annual bond
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final AnnuityCouponFixedDefinition FIXED_ANNUITY = AnnuityCouponFixedDefinition.from(CUR, SETTLEMENT_DATE, ANNUITY_TENOR, FIXED_PAYMENT_PERIOD, CALENDAR, FIXED_DAY_COUNT,
      BUSINESS_DAY, IS_EOM, NOTIONAL, RATE, FIXED_IS_PAYER);
  // Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final IborIndex INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, "Ibor");
  private static final AnnuityCouponIborDefinition IBOR_ANNUITY = AnnuityCouponIborDefinition.from(SETTLEMENT_DATE, ANNUITY_TENOR, NOTIONAL, INDEX, !FIXED_IS_PAYER, CALENDAR);
  // Swaption construction
  private static final SwapFixedIborDefinition SWAP = new SwapFixedIborDefinition(FIXED_ANNUITY, IBOR_ANNUITY);
  private static final SwaptionCashFixedIborDefinition SWAPTION_DEPRECATED = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_LONG);
  private static final SwaptionCashFixedIborDefinition SWAPTION = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_CALL, IS_LONG);
  // Conversion toDerivative
  private static final ZonedDateTime REFERENCE_DATE = DateUtils.getUTCDate(2010, 12, 27);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiryDate1() {
    SwaptionCashFixedIborDefinition.from(null, SWAP, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap1() {
    SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, null, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiryDate2() {
    SwaptionCashFixedIborDefinition.from(null, SWAP, true, IS_LONG);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap2() {
    SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, null, true, IS_LONG);
  }

  @Test
  public void testGetter() {
    assertEquals(SWAPTION.getExpiry().getExpiry(), EXPIRY_DATE);
    assertEquals(SWAPTION.getUnderlyingSwap(), SWAP);
    assertEquals(SWAPTION.isLong(), IS_LONG);
    assertEquals(SETTLEMENT_DATE, SWAPTION.getSettlementDate());
    assertEquals(SWAPTION_DEPRECATED.getExpiry().getExpiry(), EXPIRY_DATE);
    assertEquals(SWAPTION_DEPRECATED.getUnderlyingSwap(), SWAP);
    assertEquals(SWAPTION_DEPRECATED.isLong(), IS_LONG);
    assertEquals(SETTLEMENT_DATE, SWAPTION_DEPRECATED.getSettlementDate());
  }

  @Test
  public void testToDerivative() {
    final DayCount actAct = DayCounts.ACT_ACT_ISDA;
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.of(REFERENCE_DATE.toLocalDate(), LocalTime.MIDNIGHT), ZoneOffset.UTC);
    final double expiryTime = actAct.getDayCountFraction(zonedDate, EXPIRY_DATE);
    SwaptionCashFixedIbor convertedSwaption = SWAPTION_DEPRECATED.toDerivative(REFERENCE_DATE);
    assertEquals(expiryTime, convertedSwaption.getTimeToExpiry(), 1E-10);
    assertEquals(SWAPTION_DEPRECATED.getUnderlyingSwap().toDerivative(REFERENCE_DATE), convertedSwaption.getUnderlyingSwap());
    convertedSwaption = SWAPTION.toDerivative(REFERENCE_DATE);
    assertEquals(expiryTime, convertedSwaption.getTimeToExpiry(), 1E-10);
    assertEquals(SWAPTION.getUnderlyingSwap().toDerivative(REFERENCE_DATE), convertedSwaption.getUnderlyingSwap());
  }

  /**
   * Tests the equal and hashCode methods.
   */
  @Test
  public void equalHash() {
    assertTrue(SWAPTION_DEPRECATED.equals(SWAPTION_DEPRECATED));
    SwaptionCashFixedIborDefinition other = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_LONG);
    assertTrue(SWAPTION_DEPRECATED.equals(other));
    assertTrue(SWAPTION_DEPRECATED.hashCode() == other.hashCode());
    assertEquals(SWAPTION_DEPRECATED.toString(), other.toString());
    SwaptionCashFixedIborDefinition modifiedSwaption = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, !IS_LONG);
    assertFalse(SWAPTION_DEPRECATED.equals(modifiedSwaption));
    assertFalse(SWAPTION_DEPRECATED.hashCode() == modifiedSwaption.hashCode());
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(SETTLEMENT_DATE, SWAP, IS_LONG);
    assertFalse(SWAPTION_DEPRECATED.equals(modifiedSwaption));
    final IndexSwap cmsIndex = new IndexSwap(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, INDEX, ANNUITY_TENOR, CALENDAR);
    final SwapFixedIborDefinition otherSwap = SwapFixedIborDefinition.from(SETTLEMENT_DATE, cmsIndex, 2 * NOTIONAL, RATE, FIXED_IS_PAYER, CALENDAR);
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, otherSwap, IS_LONG);
    assertFalse(SWAPTION_DEPRECATED.equals(modifiedSwaption));
    assertFalse(SWAPTION_DEPRECATED.equals(EXPIRY_DATE));
    assertFalse(SWAPTION_DEPRECATED.equals(null));
    assertTrue(SWAPTION.equals(SWAPTION));
    other = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, IS_CALL, IS_LONG);
    assertTrue(SWAPTION.equals(other));
    assertTrue(SWAPTION.hashCode() == other.hashCode());
    assertEquals(SWAPTION.toString(), other.toString());
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, FIXED_IS_PAYER, !IS_LONG);
    assertFalse(SWAPTION.equals(modifiedSwaption));
    assertFalse(SWAPTION.hashCode() == modifiedSwaption.hashCode());
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, SWAP, !FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION.equals(modifiedSwaption));
    assertFalse(SWAPTION.hashCode() == modifiedSwaption.hashCode());
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(SETTLEMENT_DATE, SWAP, FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION.equals(modifiedSwaption));
    modifiedSwaption = SwaptionCashFixedIborDefinition.from(EXPIRY_DATE, otherSwap, FIXED_IS_PAYER, IS_LONG);
    assertFalse(SWAPTION.equals(modifiedSwaption));
    assertFalse(SWAPTION.equals(EXPIRY_DATE));
    assertFalse(SWAPTION.equals(null));
  }

}
