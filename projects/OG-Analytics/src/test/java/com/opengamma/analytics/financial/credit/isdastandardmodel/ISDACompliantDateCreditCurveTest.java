/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertTrue;

import org.joda.beans.BeanBuilder;
import org.joda.beans.Property;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.financial.credit.isdastandardmodel.ISDACompliantDateCreditCurve.Meta;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;

/**
 * 
 */
public class ISDACompliantDateCreditCurveTest {

  /**
   * 
   */
  @Test
  public void buildCurveTest() {
    final LocalDate baseDate = LocalDate.of(2012, 8, 8);
    final LocalDate[] dates = new LocalDate[] {LocalDate.of(2012, 12, 3), LocalDate.of(2013, 4, 29), LocalDate.of(2013, 11, 12), LocalDate.of(2014, 5, 18) };
    final double[] rates = new double[] {0.11, 0.22, 0.15, 0.09 };
    final DayCount dcc = DayCounts.ACT_365;
    final int num = dates.length;

    final ISDACompliantDateCreditCurve baseCurve = new ISDACompliantDateCreditCurve(baseDate, dates, rates);
    final LocalDate[] clonedDates = baseCurve.getCurveDates();
    assertNotSame(dates, clonedDates);
    final int modPosition = 2;
    final ISDACompliantDateCreditCurve curveWithRate = baseCurve.withRate(rates[modPosition] * 1.5, modPosition);
    final ISDACompliantDateCreditCurve clonedCurve = baseCurve.clone();
    assertNotSame(baseCurve, clonedCurve);

    final double[] t = new double[num];
    final double[] rt = new double[num];
    for (int i = 0; i < num; ++i) {
      assertEquals(dates[i], baseCurve.getCurveDate(i));
      assertEquals(dates[i], curveWithRate.getCurveDate(i));
      assertEquals(dates[i], clonedDates[i]);
      assertEquals(clonedCurve.getCurveDate(i), baseCurve.getCurveDate(i));
      if (i == modPosition) {
        assertEquals(rates[i] * 1.5, curveWithRate.getZeroRateAtIndex(i));
      }
      t[i] = dcc.getDayCountFraction(baseDate, dates[i]);
      rt[i] = t[i] * rates[i];
    }

    final LocalDate[] sampleDates = new LocalDate[] {baseDate.plusDays(2), LocalDate.of(2013, 7, 5), dates[2] };
    final int nSampleDates = sampleDates.length;
    final double[] sampleRates = new double[nSampleDates];
    final double[] fracs = new double[nSampleDates];
    for (int i = 0; i < nSampleDates; ++i) {
      fracs[i] = dcc.getDayCountFraction(baseDate, sampleDates[i]);
      sampleRates[i] = baseCurve.getZeroRate(sampleDates[i]);
    }
    assertEquals(rates[0], sampleRates[0]);
    assertEquals((rt[2] * (fracs[1] - t[1]) + rt[1] * (t[2] - fracs[1])) / (t[2] - t[1]) / fracs[1], sampleRates[1]);
    assertEquals(rates[2], sampleRates[2]);

    assertEquals(baseDate, baseCurve.getBaseDate());

    /*
     * Test meta
     */
    final Property<LocalDate> propBaseDate = baseCurve.baseDate();
    final Property<LocalDate[]> propDates = baseCurve.dates();
    final Property<DayCount> propDcc = baseCurve.dayCount();

    final Meta meta = baseCurve.metaBean();
    final BeanBuilder<?> builder = meta.builder();
    builder.set(propBaseDate.name(), baseDate);
    builder.set(propDates.name(), dates);
    builder.set(propDcc.name(), dcc);
    builder.set(meta.metaPropertyGet("name"), "");
    builder.set(meta.metaPropertyGet("t"), t);
    builder.set(meta.metaPropertyGet("rt"), rt);
    ISDACompliantDateCreditCurve builtCurve = (ISDACompliantDateCreditCurve) builder.build();
    assertEquals(baseCurve, builtCurve);

    final Meta meta1 = ISDACompliantDateCreditCurve.meta();
    assertEquals(meta1, meta);

    /*
     * hash and equals 
     */
    assertTrue(!(baseCurve.equals(null)));
    assertTrue(!(baseCurve.equals(new ISDACompliantDateCurve(baseDate, dates, rates))));
    assertTrue(!(baseCurve.equals(new ISDACompliantDateCreditCurve(baseDate.minusDays(1), dates, rates))));
    assertTrue(!(baseCurve.equals(new ISDACompliantDateCreditCurve(baseDate, new LocalDate[] {LocalDate.of(2012, 12, 3), LocalDate.of(2013, 4, 29), LocalDate.of(2013, 11, 12),
        LocalDate.of(2014, 5, 19) }, rates))));
    assertTrue(!(baseCurve.equals(new ISDACompliantDateCreditCurve(baseDate, dates, rates, DayCounts.ACT_36525))));

    assertTrue(baseCurve.equals(baseCurve));

    assertTrue(baseCurve.hashCode() != curveWithRate.hashCode());
    assertTrue(!(baseCurve.equals(curveWithRate)));

    /*
     * String
     */
    assertEquals(baseCurve.toString(), clonedCurve.toString());
  }
}
