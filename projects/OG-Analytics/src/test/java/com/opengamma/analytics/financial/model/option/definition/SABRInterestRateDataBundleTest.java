/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SABRInterestRateDataBundleTest {
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.03));
  private static final InterpolatedDoublesSurface SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10}, new double[] {0, 0, 10, 10}, new double[] {0.05, 0.05, 0.06, 0.06},
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final YieldCurveBundle CURVES = new YieldCurveBundle(new String[] {"Curve"}, new YieldAndDiscountCurve[] {CURVE});
  private static final DayCount DAYCOUNT = DayCounts.THIRTY_U_360;
  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();
  private static final SABRInterestRateParameters PARAMETERS = new SABRInterestRateParameters(SURFACE, SURFACE, SURFACE, SURFACE, DAYCOUNT, FUNCTION);
  private static final SABRInterestRateDataBundle SABR_DATA = new SABRInterestRateDataBundle(PARAMETERS, CURVES);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    new SABRInterestRateDataBundle(null, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    new SABRInterestRateDataBundle(PARAMETERS, null);
  }

  @Test
  public void testObject() {
    assertEquals(SABR_DATA.getSABRParameter(), PARAMETERS);
    SABRInterestRateDataBundle other = new SABRInterestRateDataBundle(PARAMETERS, CURVES);
    assertEquals(SABR_DATA, other);
    assertEquals(SABR_DATA.hashCode(), other.hashCode());
    other = new SABRInterestRateDataBundle(new SABRInterestRateParameters(SURFACE, SURFACE, SURFACE, SURFACE, DAYCOUNT, new SABRHaganAlternativeVolatilityFunction()), CURVES);
    assertFalse(other.equals(SABR_DATA));
    other = new SABRInterestRateDataBundle(PARAMETERS, new YieldCurveBundle(new String[] {"Curve1"}, new YieldAndDiscountCurve[] {CURVE}));
    assertFalse(other.equals(SABR_DATA));
  }
}
