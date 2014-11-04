/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

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
    /*
     * With dates
     */
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
      assertEquals(notionals[i], notionalsRes[i], baseNotional * 1.0e-12);
      assertEquals(dates[i], datesRes[i]);
      assertEquals(notionals[i], provider.getAmount(dates[i]), baseNotional * 1.0e-12);
    }

    /*
     * Without dates
     */
    VariableNotionalProvider provider1 = new VariableNotionalProvider(notionals);
    assertTrue(provider1.getDates() == null);
    for (int i = 0; i < nDates; ++i) {
      assertEquals(notionals[i], provider1.getNotionals()[i], baseNotional * 1.0e-12);
    }

    ArrayList<ZonedDateTime> list = new ArrayList<>();
    for (int i = 0; i < nDates; ++i) {
      list.add(dates[i].atTime(LocalTime.MIN).atZone(ZoneId.systemDefault()));
    }
    VariableNotionalProvider provider2 = provider1.withZonedDateTime(list);
    for (int i = 0; i < nDates; ++i) {
      assertEquals(dates[i], provider2.getDates()[i]);
      assertEquals(notionals[i], provider2.getAmount(dates[i]), baseNotional * 1.0e-12);
    }
  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void wrongDateSet() {
    double[] notionals = new double[] {100.0, 90.0 };
    ArrayList<ZonedDateTime> list = new ArrayList<>();
    list.add(ZonedDateTime.of(2014, 7, 21, 0, 0, 0, 0, ZoneId.of("UTC")));
    VariableNotionalProvider provider = new VariableNotionalProvider(notionals);
    provider.withZonedDateTime(list);
  }

  /**
   * data is null
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void missingDateSetTest() {
    double[] notionals = new double[] {100.0, 90.0 };
    VariableNotionalProvider provider = new VariableNotionalProvider(notionals);
    provider.getAmount(LocalDate.of(2014, 11, 13));
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
