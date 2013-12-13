/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.StubType;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test DayCount accrued interest.
 */
@Test(groups = TestGroup.UNIT)
public class DayCountAccruedInterestTest {

  private static final ActualActualISDA ACT_ACT_ISDA = new ActualActualISDA();
  private static final ActualActualAFB ACT_ACT_AFB = new ActualActualAFB();
  private static final ActualActualICMA ACT_ACT_ICMA = new ActualActualICMA();
  private static final ActualThreeSixty ACT_360 = new ActualThreeSixty();
  private static final ActualThreeSixtyFive ACT_365 = new ActualThreeSixtyFive();
  private static final ActualThreeSixtyFiveLong ACT_365L = new ActualThreeSixtyFiveLong();
  private static final ThirtyUThreeSixty U30_360 = new ThirtyUThreeSixty();
  private static final ThirtyEThreeSixty E30_360 = new ThirtyEThreeSixty();
  private static final ThirtyEThreeSixtyISDA E30_360_ISDA = new ThirtyEThreeSixtyISDA();
  private static final ThirtyEPlusThreeSixtyISDA E_PLUS_30_360_ISDA = new ThirtyEPlusThreeSixtyISDA();
  private static final OneOneDayCount ONE_ONE = new OneOneDayCount();
  private static final FlatDayCount FLAT = new FlatDayCount();
  private static final double EPS = 1e-9;

  @Test
  public void testISDAExample1() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2003, 11, 1);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2004, 5, 1);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (61. / 365 + 121. / 366), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (182. / 366), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear, StubType.NONE), coupon * (182. / 364), EPS);
    assertEquals(ONE_ONE.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon / paymentsPerYear, EPS);
    assertEquals(FLAT.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), 0, EPS);
  }

  @Test
  public void testISDAExample2() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(1999, 2, 1);
    final ZonedDateTime d2 = DateUtils.getUTCDate(1999, 7, 1);
    final ZonedDateTime d3 = DateUtils.getUTCDate(2000, 7, 1);
    final double coupon = 0.1;
    final int paymentsPerYear = 1;
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (150. / 365), EPS);
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (184. / 365 + 182. / 366), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (150. / 365), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (366. / 366), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear, StubType.SHORT_START), coupon * (150. / 365), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear, StubType.SHORT_START), coupon * (366. / 366), EPS);
  }

  @Test
  public void testISDAExample3() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2002, 8, 15);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2003, 7, 15);
    final ZonedDateTime d3 = DateUtils.getUTCDate(2004, 1, 15);
    final ZonedDateTime d4 = DateUtils.getUTCDate(2002, 9, 15);
    final ZonedDateTime d5 = DateUtils.getUTCDate(2003, 2, 15);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (334. / 365), EPS);
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (170. / 365 + 14. / 366), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (334. / 365), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (184. / 365), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear, StubType.LONG_START), coupon * (181. / 362 + 153. / 368), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear, StubType.LONG_START), coupon * (184. / 368), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d4, d2, coupon, paymentsPerYear, StubType.LONG_START), coupon * (31.0 / 184.0) / 2.0, EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d5, d2, coupon, paymentsPerYear, StubType.LONG_START), coupon * (153.0 / 184.0 + 31.0 / 181.0) / 2.0, EPS);
  }

  @Test
  public void testISDAExample4() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(1999, 7, 30);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2000, 1, 30);
    final ZonedDateTime d3 = DateUtils.getUTCDate(2000, 6, 30);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (155. / 365 + 29. / 366), EPS);
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (152. / 366), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (184. / 365), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear), coupon * (152. / 366), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear, StubType.SHORT_END), coupon * (184. / 368), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d2, d3, d3, coupon, paymentsPerYear, StubType.SHORT_END), coupon * (152. / 364), EPS);
  }

  @Test
  public void testISDAExample5() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(1999, 11, 30);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2000, 4, 30);
    final double coupon = 0.1;
    final int paymentsPerYear = 4;
    assertEquals(ACT_ACT_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (32. / 365 + 120. / 366), EPS);
    assertEquals(ACT_ACT_AFB.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (152. / 366), EPS);
    assertEquals(ACT_ACT_ICMA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear, StubType.LONG_END), coupon * (91. / 364 + 61. / 368), EPS);
  }

  @Test
  public void testExample1() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2007, 12, 28);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2008, 2, 28);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (62. / 360), EPS);
    assertEquals(ACT_365.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (62. / 365), EPS);
    assertEquals(ACT_365L.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (62. / 366), EPS);
    assertEquals(U30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (60. / 360), EPS);
    assertEquals(E30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (60. / 360), EPS);
    assertEquals(E30_360_ISDA.getAccruedInterest(d1, d2, coupon, true), coupon * (60. / 360), EPS);
    assertEquals(E_PLUS_30_360_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (60. / 360), EPS);
  }

  @Test
  public void testExample2() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2007, 12, 28);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2008, 2, 29);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (63. / 360), EPS);
    assertEquals(ACT_365.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (63. / 365), EPS);
    assertEquals(ACT_365L.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (63. / 366), EPS);
    assertEquals(U30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (61. / 360), EPS);
    assertEquals(E30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (61. / 360), EPS);
    assertEquals(E30_360_ISDA.getAccruedInterest(d1, d2, coupon, true), coupon * (61. / 360), EPS);
    assertEquals(E_PLUS_30_360_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (61. / 360), EPS);
  }

  @Test
  public void testExample3() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2007, 10, 31);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2008, 11, 30);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (396. / 360), EPS);
    assertEquals(ACT_365.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (396. / 365), EPS);
    assertEquals(ACT_365L.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (396. / 366), EPS);
    assertEquals(U30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (390. / 360), EPS);
    assertEquals(E30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (390. / 360), EPS);
    assertEquals(E30_360_ISDA.getAccruedInterest(d1, d2, coupon, true), coupon * (390. / 360), EPS);
    assertEquals(E_PLUS_30_360_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (390. / 360), EPS);
  }

  @Test
  public void testExample4() {
    final ZonedDateTime d1 = DateUtils.getUTCDate(2008, 2, 1);
    final ZonedDateTime d2 = DateUtils.getUTCDate(2009, 5, 31);
    final double coupon = 0.1;
    final int paymentsPerYear = 2;
    assertEquals(ACT_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (485. / 360), EPS);
    assertEquals(ACT_365.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (485. / 365), EPS);
    assertEquals(ACT_365L.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (485. / 365), EPS);
    assertEquals(U30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (480. / 360), EPS);
    assertEquals(E30_360.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (479. / 360), EPS);
    assertEquals(E30_360_ISDA.getAccruedInterest(d1, d2, coupon, true), coupon * (480. / 360), EPS);
    assertEquals(E_PLUS_30_360_ISDA.getAccruedInterest(d1, d2, d2, coupon, paymentsPerYear), coupon * (480. / 360), EPS);
  }
}
