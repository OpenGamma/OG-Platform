/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExtremeSpreadOptionDefinitionTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final Expiry PERIOD_END = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.275));
  private static final ExtremeSpreadOptionDefinition PUT = new ExtremeSpreadOptionDefinition(EXPIRY, false, PERIOD_END, false);
  private static final ExtremeSpreadOptionDefinition PUT_REVERSE = new ExtremeSpreadOptionDefinition(EXPIRY, false, PERIOD_END, true);
  private static final ExtremeSpreadOptionDefinition CALL = new ExtremeSpreadOptionDefinition(EXPIRY, true, PERIOD_END, false);
  private static final ExtremeSpreadOptionDefinition CALL_REVERSE = new ExtremeSpreadOptionDefinition(EXPIRY, true, PERIOD_END, true);
  private static final DoubleTimeSeries<?> SPOT_SERIES = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2010, 7, 1), DateUtils.getUTCDate(2010, 8, 1),
      DateUtils.getUTCDate(2010, 9, 1), DateUtils.getUTCDate(2010, 10, 1), DateUtils.getUTCDate(2010, 11, 1), DateUtils.getUTCDate(2010, 12, 1), DateUtils.getUTCDate(2011, 1, 1),
      DateUtils.getUTCDate(2011, 2, 1), DateUtils.getUTCDate(2011, 3, 1), DateUtils.getUTCDate(2011, 4, 1), DateUtils.getUTCDate(2011, 5, 1), DateUtils.getUTCDate(2011, 6, 1)}, new double[] {1, 2, 0, 1,
      4, 15, 4, 4, 0, 4, 4, 4});
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA = new StandardOptionWithSpotTimeSeriesDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.)), 0,
      new VolatilitySurface(ConstantDoublesSurface.from(0)), 2, DATE, SPOT_SERIES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPeriodEnd() {
    new ExtremeSpreadOptionDefinition(EXPIRY, true, null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPeriodEndAfterExpiry() {
    new ExtremeSpreadOptionDefinition(EXPIRY, false, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 2)), false);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    PUT.getTimeFromPeriodEnd(null);
  }

  @Test
  public void test() {
    assertEquals(PUT.getPeriodEnd(), PERIOD_END);
    assertFalse(PUT.isReverse());
    ExtremeSpreadOptionDefinition other = new ExtremeSpreadOptionDefinition(EXPIRY, false, PERIOD_END, false);
    assertEquals(other, PUT);
    assertEquals(other.hashCode(), PUT.hashCode());
    other = new ExtremeSpreadOptionDefinition(EXPIRY, false, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.15)), false);
    assertFalse(other.equals(PUT));
    other = new ExtremeSpreadOptionDefinition(EXPIRY, false, PERIOD_END, true);
    assertFalse(other.equals(PUT));
    assertEquals(PUT.getTimeFromPeriodEnd(EXPIRY.getExpiry()), 0.725, 0);
    assertEquals(PUT.getTimeFromPeriodEnd(DATE), -0.275, 0);
  }

  @Test
  public void testExercise() {
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA, null));
    assertFalse(PUT_REVERSE.getExerciseFunction().shouldExercise(DATA, null));
  }

  @Test
  public void testPayoff() {
    assertEquals(CALL.getPayoffFunction().getPayoff(DATA, null), 13, 0);
    assertEquals(CALL_REVERSE.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    assertEquals(PUT_REVERSE.getPayoffFunction().getPayoff(DATA, null), 13, 0);
  }
}
