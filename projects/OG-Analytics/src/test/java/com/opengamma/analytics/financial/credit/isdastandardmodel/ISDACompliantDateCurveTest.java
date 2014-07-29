/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantDateCurve.Meta;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ISDACompliantDateCurveTest {

  @Test
  public void test() {
  }
  /**
   *
   */
  @SuppressWarnings("unused")
  @Test
  public void cloneAndMetaTest() {
    final double tol = 1.e-12;

    final LocalDate baseDate = LocalDate.of(2013, 2, 3);
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2013, 5, 14), LocalDate.of(2013, 9, 13), LocalDate.of(2013, 9, 14), LocalDate.of(2014, 1, 23) };
    final double[] rates = new double[] {0.05, 0.06, 0.06, 0.04 };
    final int num = dates.length;
    final DayCount dcc = DayCounts.ACT_365;

    final ISDACompliantDateCurve curve365 = new ISDACompliantDateCurve(baseDate, dates, rates);

    final LocalDate[] clonedDates = curve365.getCurveDates();
    final int modPosition = num - 2;
    final ISDACompliantDateCurve rateModCurve = curve365.withRate(rates[modPosition] * 2., modPosition);

    final double[] tt = new double[num];
    final double[] rtt = new double[num];
    for (int i = 0; i < num; ++i) {
      assertEquals(0, clonedDates[i].compareTo(dates[i]));
      assertEquals(0, curve365.getCurveDate(i).compareTo(dates[i]));
      if (i == modPosition) {
        assertEquals(rates[i] * 2., rateModCurve.getYData()[i]);
      } else {
        assertEquals(rates[i], rateModCurve.getYData()[i]);
      }
      tt[i] = dcc.getDayCountFraction(baseDate, dates[i]);
      rtt[i] = tt[i] * rates[i];
    }
    assertNotSame(clonedDates, dates);

    final LocalDate[] sampleDates = new LocalDate[] {LocalDate.of(2013, 2, 13), LocalDate.of(2013, 11, 19), LocalDate.of(2014, 1, 23) };
    assertEquals(rates[0], curve365.getZeroRate(sampleDates[0]));
    final double t = dcc.getDayCountFraction(baseDate, sampleDates[1]);
    final double refT2 = dcc.getDayCountFraction(baseDate, dates[2]);
    final double refT3 = dcc.getDayCountFraction(baseDate, dates[3]);
    assertEquals((rates[2] * refT2 * (refT3 - t) + rates[3] * refT3 * (t - refT2)) / (refT3 - refT2) / t, curve365.getZeroRate(sampleDates[1]), tol);
    assertEquals(rates[3], curve365.getZeroRate(sampleDates[2]));

    assertEquals(baseDate, curve365.getBaseDate());
    final Property<LocalDate> propBase = curve365.baseDate();
    assertEquals(baseDate, propBase.get());
    final Property<LocalDate[]> propDates = curve365.dates();
    for (int i = 0; i < num; ++i) {
      assertEquals(dates[i], propDates.get()[i]);
    }
    final Property<DayCount> propDcc = curve365.dayCount();
    assertEquals(dcc, propDcc.get());

    final ISDACompliantDateCurve clonedCurve = curve365.clone();
    assertNotSame(clonedCurve, curve365);
    assertTrue(clonedCurve.equals(curve365));
    assertEquals(clonedCurve.hashCode(), curve365.hashCode());
    assertEquals(clonedCurve.toString(), curve365.toString());

    final Meta meta = ISDACompliantDateCurve.meta();
    final Meta metafb = curve365.metaBean();
    assertEquals(meta, metafb);
    final BeanBuilder<?> builder = meta.builder();
    final Map<String, MetaProperty<?>> map = meta.metaPropertyMap();

    final MetaProperty<LocalDate> propBaseDate = meta.baseDate();
    final MetaProperty<LocalDate[]> metaDates = meta.dates();
    final MetaProperty<DayCount> metaDcc = meta.dayCount();

    builder.set(propBaseDate.name(), baseDate);
    builder.set(propDates.name(), dates);
    builder.set(propDcc.name(), dcc);
    builder.set(meta.metaPropertyGet("name"), "");
    builder.set(meta.metaPropertyGet("t"), tt);
    builder.set(meta.metaPropertyGet("rt"), rtt);
    final ISDACompliantDateCurve builtCurve = (ISDACompliantDateCurve) builder.build();
    assertEquals(curve365, builtCurve);

    /*
     * Errors expected
     */
    try {
      final double[] ratesShort = Arrays.copyOf(rates, num - 1);
      new ISDACompliantDateCurve(baseDate, dates, ratesShort);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
    try {
      dates[1] = LocalDate.of(2015, 1, 22);
      new ISDACompliantDateCurve(baseDate, dates, rates);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  /**
   *
   */
  @Test
  public void hashEqualsTest() {
    final LocalDate baseDate = LocalDate.of(2009, 2, 3);
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2012, 5, 14), LocalDate.of(2013, 6, 13), LocalDate.of(2013, 9, 14) };
    final double[] rates = new double[] {0.05, 0.06, 0.04 };
    final int num = dates.length;
    final DayCount dcc = DayCounts.ACT_360;

    final ISDACompliantDateCurve curve360 = new ISDACompliantDateCurve(baseDate, dates, rates, dcc);
    final ISDACompliantDateCurve curve365 = new ISDACompliantDateCurve(baseDate, dates, rates);
    final ISDACompliantDateCurve curve360a = new ISDACompliantDateCurve(baseDate, new LocalDate[] {LocalDate.of(2012, 5, 14), LocalDate.of(2013, 7, 13), LocalDate.of(2013, 9, 14) }, rates, dcc);
    final ISDACompliantDateCurve curve360b = new ISDACompliantDateCurve(baseDate.minusDays(6), dates, rates, dcc);
    final ISDACompliantDateCurve curve360c = new ISDACompliantDateCurve(baseDate, dates, new double[] {0.05, 0.05, 0.04 }, dcc);
    assertTrue(curve360.equals(curve360));

    assertTrue(curve360.hashCode() != curve365.hashCode());
    assertTrue(!(curve360.equals(curve365)));

    assertTrue(curve360.hashCode() != curve360a.hashCode());
    assertTrue(!(curve360.equals(curve360a)));

    assertTrue(curve360.hashCode() != curve360b.hashCode());
    assertTrue(!(curve360.equals(curve360b)));

    assertTrue(curve360.hashCode() != curve360c.hashCode());
    assertTrue(!(curve360.equals(curve360c)));

    assertTrue(!(curve360.equals(null)));
    assertTrue(!(curve360.equals(new ISDACompliantDateYieldCurve(baseDate, dates, rates, dcc))));
  }
}
