/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link VariableNotionalProvider}
 */
@SuppressWarnings("unused")
@Test(groups = TestGroup.UNIT)
public class VariableNotionalProviderTest {

  /**
   * Test methods and private variables
   */
  @Test
  public void generalTest() {
    LocalDate baseDate = LocalDate.of(2014, 7, 18);
    double baseNotional = 1.0e6;
    Period period = Period.ofMonths(3);
    LocalDate[] dates = new LocalDate[] {baseDate, baseDate.plus(period), baseDate.plus(period.multipliedBy(2)),
        baseDate.plus(period.multipliedBy(3)), baseDate.plus(period.multipliedBy(4)) };
    int nDates = dates.length;
    double[] notionals = new double[] {baseNotional, baseNotional * 0.9, baseNotional * 0.8, baseNotional * 0.7,
        baseNotional * 0.6 };
    VariableNotionalProvider provider = new VariableNotionalProvider(dates, notionals);

    LocalDate[] datesRes = provider.getDates();
    double[] notionalsRes = provider.getNotionals();
    assertEquals(nDates, datesRes.length);
    assertEquals(nDates, notionalsRes.length);
    for (int i = 0; i < nDates; ++i) {
      assertEquals(notionals[i], provider.getAmount(dates[i]), baseNotional * 1.0e-12);
    }
  }

  /**
   * date is empty
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void emptyDateTest() {
    LocalDate[] dates = new LocalDate[] {};
    double[] notionals = new double[] {100.0 };
    new VariableNotionalProvider(dates, notionals);
  }

  /**
   * notional is empty
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void emptyNotionalTest() {
    LocalDate[] dates = new LocalDate[] {LocalDate.of(2014, 7, 18) };
    double[] notionals = new double[] {};
    new VariableNotionalProvider(dates, notionals);
  }

  /**
   * notionals and dates, different lengths
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notSameLengthTest() {
    LocalDate[] dates = new LocalDate[] {LocalDate.of(2014, 7, 18) };
    double[] notionals = new double[] {100.0, 200.0 };
    new VariableNotionalProvider(dates, notionals);
  }

  /**
   * date is not found 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void dateNotFoundTest() {
    LocalDate[] dates = new LocalDate[] {LocalDate.of(2014, 7, 18), LocalDate.of(2014, 9, 18) };
    double[] notionals = new double[] {100.0, 200.0 };
    VariableNotionalProvider provider = new VariableNotionalProvider(dates, notionals);
    provider.getAmount(LocalDate.of(2014, 8, 1));
  }
}
