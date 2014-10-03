/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageFixingDatesDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborAverageIndexDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingFlatSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSimpleSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborCompoundingSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborRatchetDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONArithmeticAverageSpreadSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONSpreadSimplifiedDefinition;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Unit test for {@link CouponTenorVisitor}.
 */
public class CouponTenorVisitorTest {
  
  private static final InstrumentDefinitionVisitor<Void, Set<Tenor>> INSTANCE = CouponTenorVisitor.getInstance();
  
  private static final ZonedDateTime START_DATE = ZonedDateTime.of(LocalDate.of(2014, 1, 18), LocalTime.NOON, ZoneId.systemDefault());
  
  private static final ZonedDateTime END_DATE = ZonedDateTime.of(LocalDate.of(2014, 3, 18), LocalTime.NOON, ZoneId.systemDefault());
  
  private static final IborIndex USD_LIBOR_3M = new IborIndex(Currency.USD,
                                                              Period.ofMonths(3),
                                                              0,
                                                              DayCounts.ACT_360,
                                                              BusinessDayConventions.MODIFIED_FOLLOWING,
                                                              true,
                                                              "");
  
  private static final IborIndex USD_LIBOR_6M = new IborIndex(Currency.USD,
                                                              Period.ofMonths(6),
                                                              0,
                                                              DayCounts.ACT_360,
                                                              BusinessDayConventions.MODIFIED_FOLLOWING,
                                                              true,
                                                              "");
  
  private static final IndexON USD_FEDFUNDS = new IndexON("USD FED FUNDS",
                                                          Currency.USD,
                                                          DayCounts.ACT_360,
                                                          1);
  
  private static final Calendar TEST_CALENDAR = new MondayToFridayCalendar("");
  
  private static final CouponIborDefinition USD_LIBOR_3M_COUPON =
      CouponIborDefinition.from(1, START_DATE, USD_LIBOR_3M, TEST_CALENDAR);
  
  @Test
  public void testCouponIborDefinition() {
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(USD_LIBOR_3M_COUPON.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborSpreadDefinition() {
    CouponIborSpreadDefinition coupon =        
        CouponIborSpreadDefinition.from(USD_LIBOR_3M_COUPON, 0.01);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborGearingDefinition() {
    CouponIborGearingDefinition coupon =
        CouponIborGearingDefinition.from(USD_LIBOR_3M_COUPON, 0., 1.);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborCompoundingDefinition() {
    CouponIborCompoundingDefinition coupon =
        CouponIborCompoundingDefinition.from(1, START_DATE, Period.ofMonths(1), USD_LIBOR_3M, TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborCompoundingFlatSpreadDefinition() {
    CouponIborCompoundingFlatSpreadDefinition coupon = CouponIborCompoundingFlatSpreadDefinition.from(1,
                                                                                                      START_DATE,
                                                                                                      END_DATE,
                                                                                                      USD_LIBOR_3M,
                                                                                                      0.01,
                                                                                                      StubType.NONE,
                                                                                                      BusinessDayConventions.MODIFIED_FOLLOWING,
                                                                                                      true,
                                                                                                      TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborAverageIndexDefinition() {
    CouponIborAverageIndexDefinition coupon =
        CouponIborAverageIndexDefinition.from(USD_LIBOR_3M_COUPON,
                                              END_DATE, USD_LIBOR_3M, USD_LIBOR_6M, 1, 1, TEST_CALENDAR, TEST_CALENDAR);
    assertEquals("Expected two tenors", Sets.newHashSet(Tenor.of(USD_LIBOR_3M.getTenor()),
                                                        Tenor.of(USD_LIBOR_6M.getTenor())),
                 coupon.accept(INSTANCE));
  }
  
  @Test
  public void testCouponIborAverageFixingDatesCompoundingDefinition() {
    double accrualFactor = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponIborAverageFixingDatesCompoundingDefinition coupon =
        CouponIborAverageFixingDatesCompoundingDefinition.from(Currency.USD,
                                                               END_DATE,
                                                               START_DATE,
                                                               END_DATE,
                                                               accrualFactor,
                                                               1.,
                                                               new double[] { accrualFactor },
                                                               USD_LIBOR_3M,
                                                               new ZonedDateTime[][] {{ START_DATE }},
                                                               new double[][] {{ 1. }},
                                                               TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }

  @Test
  public void testCouponIborAverageFixingDatesDefinition() {
    double accrualFactor = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponIborAverageFixingDatesDefinition coupon =
        CouponIborAverageFixingDatesDefinition.from(Currency.USD,
                                                    END_DATE,
                                                    START_DATE,
                                                    END_DATE,
                                                    accrualFactor,
                                                    1.,
                                                    USD_LIBOR_3M,
                                                    new ZonedDateTime[] { START_DATE },
                                                    new double[] { 1. },
                                                    TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }

  @Test
  public void testCouponIborAverageFixingDatesCompoundingFlatSpreadDefinition() {
    double accrualFactor = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition coupon =
        CouponIborAverageFixingDatesCompoundingFlatSpreadDefinition.from(Currency.USD,
                                                                         END_DATE,
                                                                         START_DATE,
                                                                         END_DATE,
                                                                         accrualFactor,
                                                                         1.,
                                                                         new double[] { accrualFactor },
                                                                         USD_LIBOR_3M,
                                                                         new ZonedDateTime[][] {{ START_DATE }},
                                                                         new double[][] {{ 1. }},
                                                                         TEST_CALENDAR,
                                                                         0.01);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborCompoundingSimpleSpreadDefinition() {
    double accrualFactor = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponIborCompoundingSimpleSpreadDefinition coupon =
        CouponIborCompoundingSimpleSpreadDefinition.from(END_DATE,
                                                         1.,
                                                         USD_LIBOR_3M,
                                                         new ZonedDateTime[] { START_DATE },
                                                         new ZonedDateTime[] { END_DATE },
                                                         new double[] { accrualFactor },
                                                         0.01,
                                                         TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponIborRatchetDefinition() {
    double accrualFactor = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponIborRatchetDefinition coupon =
        new CouponIborRatchetDefinition(Currency.USD,
                                        END_DATE,
                                        START_DATE,
                                        END_DATE,
                                        accrualFactor,
                                        1.,
                                        START_DATE,
                                        USD_LIBOR_3M,
                                        new double[] { 1., 2., 3. },
                                        new double[] { 1., 2., 3. },
                                        new double[] { 1., 2., 3. },
                                        TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponONDefinition() {
    CouponONDefinition coupon = CouponONDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 1, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponONSimplifiedDefinition() {
    CouponONSimplifiedDefinition coupon =
        CouponONSimplifiedDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1., 0, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponArithmeticAverageONDefinition() {
    CouponONArithmeticAverageDefinition coupon =
        CouponONArithmeticAverageDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 0, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponArithmeticAverageONSpreadDefinition() {
    CouponONArithmeticAverageSpreadDefinition coupon =
        CouponONArithmeticAverageSpreadDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 0, 0.01, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponArithmeticAverageONSpreadSimplifiedDefinition() {
    CouponONArithmeticAverageSpreadSimplifiedDefinition coupon =
        CouponONArithmeticAverageSpreadSimplifiedDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 0, 0.01, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponONSpreadDefinition() {
    CouponONSpreadDefinition coupon =
        CouponONSpreadDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 0, TEST_CALENDAR, 0.01);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponONSpreadSimplifiedDefinition() {
    CouponONSpreadSimplifiedDefinition coupon =
        CouponONSpreadSimplifiedDefinition.from(USD_FEDFUNDS, START_DATE, END_DATE, 1, 0.01, 1, TEST_CALENDAR);
    assertEquals("Expected ON tenor", Tenor.ON, Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponFixedDefinition() {
    double paymentYearFrac = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponFixedDefinition coupon =
        CouponFixedDefinition.from(Currency.USD, END_DATE, START_DATE, END_DATE, paymentYearFrac, 1, 0.01);
    assertTrue("Expected no tenor", coupon.accept(INSTANCE).isEmpty());
  }

  @Test
  public void testCouponIborCompoundingSpreadDefinition() {    
    CouponIborCompoundingSpreadDefinition coupon = 
        CouponIborCompoundingSpreadDefinition.from(1.0, // notional
                                                   START_DATE, // accrualStartDate
                                                   END_DATE, // accrualEndDate
                                                   USD_LIBOR_3M,
                                                   0.01, // spread
                                                   StubType.NONE,
                                                   BusinessDayConventions.MODIFIED_FOLLOWING,
                                                   true,
                                                   TEST_CALENDAR);
    assertEquals("Expected index tenor",
                 Tenor.of(USD_LIBOR_3M.getTenor()),
                 Iterables.getOnlyElement(coupon.accept(INSTANCE)));
  }
  
  @Test
  public void testCouponFixedAccruedCompoundingDefinition() {
    double paymentYearFrac = TimeCalculator.getTimeBetween(START_DATE, END_DATE);
    CouponFixedAccruedCompoundingDefinition coupon =
        CouponFixedAccruedCompoundingDefinition.from(Currency.USD,
                                                     END_DATE,
                                                     START_DATE,
                                                     END_DATE,
                                                     paymentYearFrac,
                                                     1.,
                                                     0.01,
                                                     TEST_CALENDAR);
    assertTrue("Expected no tenor", coupon.accept(INSTANCE).isEmpty());
  }
}
