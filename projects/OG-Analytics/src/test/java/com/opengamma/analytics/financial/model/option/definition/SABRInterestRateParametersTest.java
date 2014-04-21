/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.test.TestGroup;

/**
 * Test the SABR parameters surfaces object.
 */
@Test(groups = TestGroup.UNIT)
public class SABRInterestRateParametersTest {

  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final InterpolatedDoublesSurface ALPHA_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10 }, new double[] {0, 0, 10, 10 }, new double[] {0.2, 0.2, 0.2, 0.2 },
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface BETA_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10 }, new double[] {0, 0, 10, 10 }, new double[] {1, 1, 1, 1 },
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface RHO_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10 }, new double[] {0, 0, 10, 10 }, new double[] {-0.5, -0.5, -0.5, -0.5 },
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface NU_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10 }, new double[] {0, 0, 10, 10 }, new double[] {0.5, 0.5, 0.5, 0.5 },
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();
  private static final SABRInterestRateParameters PARAMETERS = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha1() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha2() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, (VolatilityFunctionProvider<SABRFormulaData>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    PARAMETERS.getVolatility(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongData() {
    PARAMETERS.getVolatility(new double[] {1, 2, 3 });
  }

  @Test
  public void hashEqualGetter() {
    assertEquals(PARAMETERS.getAlphaSurface(), ALPHA_SURFACE);
    assertEquals(PARAMETERS.getBetaSurface(), BETA_SURFACE);
    assertEquals(PARAMETERS.getRhoSurface(), RHO_SURFACE);
    assertEquals(PARAMETERS.getNuSurface(), NU_SURFACE);
    assertEquals(PARAMETERS.getSabrFunction(), FUNCTION);
    SABRInterestRateParameters other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertEquals(PARAMETERS, other);
    assertEquals(PARAMETERS.hashCode(), other.hashCode());
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE);
    assertTrue(other.equals(PARAMETERS));
    other = new SABRInterestRateParameters(BETA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, ALPHA_SURFACE, RHO_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, ALPHA_SURFACE, NU_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, ALPHA_SURFACE, FUNCTION);
    assertFalse(other.equals(PARAMETERS));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, new SABRHaganAlternativeVolatilityFunction());
    assertFalse(other.equals(PARAMETERS));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCorrelation() {
    new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, null);
  }

  @Test
  public void correlationGetter() {
    final double correlation = 0.50;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation }); // Constant function
    final SABRInterestRateCorrelationParameters sabrCorrelation = new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, correlationFunction);
    assertEquals("SABR with correlation: get correlation", correlationFunction, sabrCorrelation.getCorrelation());
  }

  // Deprecated tests.

  private static final DayCount DAYCOUNT = DayCounts.THIRTY_U_360;

  private static final SABRInterestRateParameters OBJECT_DEP = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha1Dep() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha2Dep() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta1Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta2Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho1Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho2Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu1Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu2Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount1Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, (DayCount) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount2Dep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, null, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunctionDep() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, null);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testObjectDep() {
    assertEquals(OBJECT_DEP.getAlphaSurface(), ALPHA_SURFACE);
    assertEquals(OBJECT_DEP.getBetaSurface(), BETA_SURFACE);
    assertEquals(OBJECT_DEP.getRhoSurface(), RHO_SURFACE);
    assertEquals(OBJECT_DEP.getNuSurface(), NU_SURFACE);
    assertEquals(OBJECT_DEP.getDayCount(), DAYCOUNT);
    assertEquals(OBJECT_DEP.getSabrFunction(), FUNCTION);
    SABRInterestRateParameters other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertEquals(OBJECT_DEP, other);
    assertEquals(OBJECT_DEP.hashCode(), other.hashCode());
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
    assertTrue(other.equals(OBJECT_DEP));
    other = new SABRInterestRateParameters(BETA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT_DEP));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, ALPHA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT_DEP));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, ALPHA_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT_DEP));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, ALPHA_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT_DEP));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, new SABRHaganAlternativeVolatilityFunction());
    assertFalse(other.equals(OBJECT_DEP));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCorrelationDep() {
    new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, null);
  }

  @Test
  public void correlationGetterDep() {
    final double correlation = 0.50;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation }); // Constant function
    final SABRInterestRateCorrelationParameters sabrCorrelation = new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, correlationFunction);
    assertEquals("SABR with correlation: get correlation", correlationFunction, sabrCorrelation.getCorrelation());
  }

}
