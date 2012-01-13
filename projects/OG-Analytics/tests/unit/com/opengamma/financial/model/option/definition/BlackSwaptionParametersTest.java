/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.instrument.index.GeneratorSwap;
import com.opengamma.financial.instrument.index.generator.EUR1YEURIBOR3M;
import com.opengamma.financial.instrument.index.generator.EUR1YEURIBOR6M;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.math.interpolation.GridInterpolator2D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

public class BlackSwaptionParametersTest {

  /**
   * The linear interpolator/ flat extrapolator.  Used for SABR parameters interpolation.
   */
  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final Calendar CALENDAR = new MondayToFridayCalendar("TARGET");
  private static final GeneratorSwap EUR1YEURIBOR6M = new EUR1YEURIBOR6M(CALENDAR);
  private static final InterpolatedDoublesSurface BLACK_SURFACE = InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0}, new double[] {2, 2, 2, 10, 10, 10}, new double[] {0.35,
      0.34, 0.25, 0.30, 0.25, 0.20}, INTERPOLATOR_2D);
  private static final BlackSwaptionParameters BLACK_SWAPTION = new BlackSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR6M);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBlack() {
    new BlackSwaptionParameters(null, EUR1YEURIBOR6M);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGenerator() {
    new BlackSwaptionParameters(BLACK_SURFACE, null);
  }

  @Test
  /**
   * Tests the object getters.
   */
  public void getter() {
    assertEquals("Black Swaption Surface: getter", BLACK_SURFACE, BLACK_SWAPTION.getVolatilitySurface());
    assertEquals("Black Swaption Surface: getter", EUR1YEURIBOR6M, BLACK_SWAPTION.getGeneratorSwap());
  }

  @Test
  /**
   * Tests the object equal and hash code methods.
   */
  public void equalHash() {
    assertTrue("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(BLACK_SWAPTION));
    final BlackSwaptionParameters other = new BlackSwaptionParameters(BLACK_SURFACE, EUR1YEURIBOR6M);
    assertTrue("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(other));
    assertEquals("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.hashCode(), other.hashCode());
    BlackSwaptionParameters modified = new BlackSwaptionParameters(BLACK_SURFACE, new EUR1YEURIBOR3M(CALENDAR));
    assertFalse("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(modified));
    InterpolatedDoublesSurface surface2 = InterpolatedDoublesSurface.from(new double[] {0.5, 1.0, 5.0, 0.5, 1.0, 5.0}, new double[] {2, 2, 2, 10, 10, 10}, new double[] {0.35, 0.34, 0.25, 0.31, 0.25,
        0.20}, INTERPOLATOR_2D);
    modified = new BlackSwaptionParameters(surface2, EUR1YEURIBOR6M);
    assertFalse("Black Swaption Surface: equal and hash code", BLACK_SWAPTION.equals(modified));
  }

}
