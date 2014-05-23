/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TwentyEightThreeSixtyTest extends DayCountTestCase {
  private static final TwentyEightThreeSixty DC = new TwentyEightThreeSixty();

  @Override
  protected DayCount getDayCount() {
    return DC;
  }

  @Test
  public void test() {
    assertEquals(COUPON * DC.getDayCountFraction(D1, D2), DC.getAccruedInterest(D1, D2, D3, COUPON, PAYMENTS), 0);
    assertEquals(DC.getName(), "28/360");
    final LocalDate d1 = LocalDate.of(2012, 7, 2);
    final LocalDate d2 = LocalDate.of(2012, 7, 28);
    final LocalDate d3 = LocalDate.of(2012, 8, 2);
    final LocalDate d4 = LocalDate.of(2012, 8, 28);
    final LocalDate d5 = LocalDate.of(2013, 8, 2);
    final LocalDate d6 = LocalDate.of(2013, 8, 28);
    final LocalDate d7 = LocalDate.of(2012, 6, 29);
    final LocalDate d8 = LocalDate.of(2012, 7, 31);
    final LocalDate d9 = LocalDate.of(2012, 8, 31);
    final LocalDate d10 = LocalDate.of(2013, 8, 31);
    final LocalDate d11 = LocalDate.of(2014, 1, 31);
    assertEquals(26. / 360, DC.getDayCountFraction(d1, d2), 0);
    assertEquals(28. / 360, DC.getDayCountFraction(d1, d3), 0);
    assertEquals(54. / 360, DC.getDayCountFraction(d1, d4), 0);
    assertEquals(1 + 28. / 360, DC.getDayCountFraction(d1, d5), 0);
    assertEquals(1 + 54. / 360, DC.getDayCountFraction(d1, d6), 0);
    assertEquals(28. / 360, DC.getDayCountFraction(d7, d2), 0);
    assertEquals(30. / 360, DC.getDayCountFraction(d7, d3), 0);
    assertEquals(56. / 360, DC.getDayCountFraction(d7, d4), 0);
    assertEquals(1 + 30. / 360, DC.getDayCountFraction(d7, d5), 0);
    assertEquals(1 + 56. / 360, DC.getDayCountFraction(d7, d6), 0);
    assertEquals(26. / 360, DC.getDayCountFraction(d1, d8), 0);
    assertEquals(28. / 360, DC.getDayCountFraction(d7, d8), 0);
    assertEquals(54. / 360, DC.getDayCountFraction(d1, d9), 0);
    assertEquals(56. / 360, DC.getDayCountFraction(d7, d9), 0);
    assertEquals(1 + 54. / 360, DC.getDayCountFraction(d1, d10), 0);
    assertEquals(1 + 56. / 360, DC.getDayCountFraction(d7, d10), 0);
    assertEquals(140. / 360, DC.getDayCountFraction(d10, d11), 0);
  }
}
