/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.math.function.DoubleFunction1D;
import com.opengamma.math.function.RealPolynomialFunction1D;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

/**
 * 
 */
public class SABRInterestRateParametersTest {
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final InterpolatedDoublesSurface ALPHA_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10}, new double[] {0, 0, 10, 10}, new double[] {0.2, 0.2, 0.2, 0.2},
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface BETA_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10}, new double[] {0, 0, 10, 10}, new double[] {1, 1, 1, 1},
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface RHO_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10}, new double[] {0, 0, 10, 10}, new double[] {-0.5, -0.5, -0.5, -0.5},
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final InterpolatedDoublesSurface NU_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.0, 10, 0.0, 10}, new double[] {0, 0, 10, 10}, new double[] {0.5, 0.5, 0.5, 0.5},
      new GridInterpolator2D(LINEAR, LINEAR));
  private static final DayCount DAYCOUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();
  private static final SABRInterestRateParameters OBJECT = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha1() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAlpha2() {
    new SABRInterestRateParameters(null, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBeta2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, null, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRho2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, null, NU_SURFACE, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null, DAYCOUNT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullNu2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, null, DAYCOUNT, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount1() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDayCount2() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, null, FUNCTION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    OBJECT.getVolatility(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongData() {
    OBJECT.getVolatility(new double[] {1, 2, 3});
  }

  @Test
  public void testObject() {
    assertEquals(OBJECT.getAlphaSurface(), ALPHA_SURFACE);
    assertEquals(OBJECT.getBetaSurface(), BETA_SURFACE);
    assertEquals(OBJECT.getRhoSurface(), RHO_SURFACE);
    assertEquals(OBJECT.getNuSurface(), NU_SURFACE);
    assertEquals(OBJECT.getDayCount(), DAYCOUNT);
    assertEquals(OBJECT.getSabrFunction(), FUNCTION);
    SABRInterestRateParameters other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertEquals(OBJECT, other);
    assertEquals(OBJECT.hashCode(), other.hashCode());
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(BETA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, ALPHA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, ALPHA_SURFACE, NU_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, ALPHA_SURFACE, DAYCOUNT, FUNCTION);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DayCountFactory.INSTANCE.getDayCount("Act/365"), FUNCTION);
    assertFalse(other.equals(OBJECT));
    other = new SABRInterestRateParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, new SABRHaganAlternativeVolatilityFunction());
    assertFalse(other.equals(OBJECT));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCorrelation() {
    new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, null);
  }

  @Test
  public void correlationGetter() {
    double correlation = 0.50;
    final DoubleFunction1D correlationFunction = new RealPolynomialFunction1D(new double[] {correlation}); // Constant function
    SABRInterestRateCorrelationParameters sabrCorrelation = new SABRInterestRateCorrelationParameters(ALPHA_SURFACE, BETA_SURFACE, RHO_SURFACE, NU_SURFACE, DAYCOUNT, correlationFunction);
    assertEquals("SABR with correlation: get correlation", correlationFunction, sabrCorrelation.getCorrelation());
  }
}
