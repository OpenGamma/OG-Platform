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
import com.opengamma.analytics.financial.model.volatility.surface.DriftSurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneralLogNormalOptionDataBundleTest {
  private static final YieldCurve YIELD = YieldCurve.from(ConstantDoublesCurve.from(0.03));
  private static final DriftSurface DRIFT_SURFACE = new DriftSurface(FunctionalDoublesSurface.from(new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      final double f = x[1];
      if (f > 120) {
        return 0.05;
      }
      return 0.04;
    }

  }));
  private static final VolatilitySurface VOLATILITY_SURFACE = new VolatilitySurface(FunctionalDoublesSurface.from(new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... x) {
      final double f = x[1];
      if (f > 120) {
        return 0.5;
      }
      return 0.4;
    }

  }));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2011, 1, 1);
  private static final GeneralLogNormalOptionDataBundle DATA = new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDriftSurface() {
    new GeneralLogNormalOptionDataBundle(YIELD, null, VOLATILITY_SURFACE, SPOT, DATE);
  }

  @Test
  public void test() {
    final double f = 130;
    final double t = 0.5;
    assertEquals(DATA.getLocalVolatility(f, t), VOLATILITY_SURFACE.getVolatility(DoublesPair.of(t, f)), 0);
    assertEquals(DATA.getLocalVolatility(f, t), DATA.getLocalVolatility(f, t), 0);
    assertEquals(DATA.getDate(), DATE);
    assertEquals(DATA.getDriftSurface(), DRIFT_SURFACE);
    assertEquals(DATA.getSpot(), SPOT, 0);
    assertEquals(DATA.getVolatilitySurface(), VOLATILITY_SURFACE);
    assertEquals(DATA.getInterestRateCurve(), YIELD);
    GeneralLogNormalOptionDataBundle other = new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new GeneralLogNormalOptionDataBundle(DATA);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new GeneralLogNormalOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.02)), DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new GeneralLogNormalOptionDataBundle(YIELD, new DriftSurface(ConstantDoublesSurface.from(0.01)), VOLATILITY_SURFACE, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, new VolatilitySurface(ConstantDoublesSurface.from(0.4)), SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT + 1, DATE);
    assertFalse(other.equals(DATA));
    other = new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, DATE.plusDays(1));
    assertFalse(other.equals(DATA));
  }

  @Test
  public void testBuilders() {
    final ZonedDateTime newDate = DATE.plusDays(1);
    assertEquals(DATA.withDate(newDate), new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, newDate));
    final YieldCurve newCurve = YieldCurve.from(ConstantDoublesCurve.from(0.05));
    assertEquals(DATA.withInterestRateCurve(newCurve), new GeneralLogNormalOptionDataBundle(newCurve, DRIFT_SURFACE, VOLATILITY_SURFACE, SPOT, DATE));
    final VolatilitySurface newSurface = new VolatilitySurface(ConstantDoublesSurface.from(0.9));
    assertEquals(DATA.withVolatilitySurface(newSurface), new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, newSurface, SPOT, DATE));
    final DriftSurface newDrift = new DriftSurface(ConstantDoublesSurface.from(0.9));
    assertEquals(DATA.withDriftSurface(newDrift), new GeneralLogNormalOptionDataBundle(YIELD, newDrift, VOLATILITY_SURFACE, SPOT, DATE));
    final double newSpot = SPOT + 1;
    assertEquals(DATA.withSpot(newSpot), new GeneralLogNormalOptionDataBundle(YIELD, DRIFT_SURFACE, VOLATILITY_SURFACE, newSpot, DATE));
  }
}
