package com.opengamma.sesame.marketdata.scenarios;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.util.time.Tenor;

@Test
public class VolatilitySurfaceExpiryShiftsTestTest {

  private static final GridInterpolator2D INTERPOLATOR =
      new GridInterpolator2D(
          Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR),
          Interpolator1DFactory.getInterpolator(Interpolator1DFactory.LINEAR),
          Interpolator1DFactory.getInterpolator(Interpolator1DFactory.FLAT_EXTRAPOLATOR),
          Interpolator1DFactory.getInterpolator(Interpolator1DFactory.FLAT_EXTRAPOLATOR));
  private static final double[] X = new double[]{1, 2, 3, 2, 3, 4, 3, 4};
  private static final double[] Y = new double[]{1, 1, 1, 2, 2, 2, 3, 3};
  private static final double[] Z = new double[]{1, 2, 3, 4, 5, 6, 7, 8};
  private static final List<Tenor> TENORS =
      ImmutableList.of(
          Tenor.ONE_MONTH,
          Tenor.ONE_MONTH,
          Tenor.ONE_MONTH,
          Tenor.TWO_MONTHS,
          Tenor.TWO_MONTHS,
          Tenor.TWO_MONTHS,
          Tenor.THREE_MONTHS,
          Tenor.THREE_MONTHS);
  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(X, Y, Z, INTERPOLATOR);
  private static final VolatilitySurface VOL_SURFACE = new VolatilitySurface(SURFACE, TENORS);
  private static final double TOLERANCE = 1e-6;

  public void absolute() {
    Map<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_MONTH, 0.1, Tenor.THREE_MONTHS, 0.2);
    VolatilitySurfaceExpiryShifts perturbation = VolatilitySurfaceExpiryShifts.absolute(shifts);
    VolatilitySurface shiftedSurface = (VolatilitySurface) perturbation.apply(VOL_SURFACE, StandardMatchDetails.MATCH);
    assertEquals(1.1, shiftedSurface.getVolatility(1, 1));
    assertEquals(2.1, shiftedSurface.getVolatility(2, 1));
    assertEquals(3.1, shiftedSurface.getVolatility(3, 1));
    assertEquals(4.0, shiftedSurface.getVolatility(2, 2));
    assertEquals(5.0, shiftedSurface.getVolatility(3, 2));
    assertEquals(6.0, shiftedSurface.getVolatility(4, 2));
    assertEquals(7.2, shiftedSurface.getVolatility(3, 3));
    assertEquals(8.2, shiftedSurface.getVolatility(4, 3));
  }

  public void relative() {
    Map<Tenor, Double> shifts = ImmutableMap.of(Tenor.ONE_MONTH, 0.1, Tenor.THREE_MONTHS, 0.2);
    VolatilitySurfaceExpiryShifts perturbation = VolatilitySurfaceExpiryShifts.relative(shifts);
    VolatilitySurface shiftedSurface = (VolatilitySurface) perturbation.apply(VOL_SURFACE, StandardMatchDetails.MATCH);
    assertEquals(1.1, shiftedSurface.getVolatility(1, 1), TOLERANCE);
    assertEquals(2.2, shiftedSurface.getVolatility(2, 1), TOLERANCE);
    assertEquals(3.3, shiftedSurface.getVolatility(3, 1), TOLERANCE);
    assertEquals(4.0, shiftedSurface.getVolatility(2, 2), TOLERANCE);
    assertEquals(5.0, shiftedSurface.getVolatility(3, 2), TOLERANCE);
    assertEquals(6.0, shiftedSurface.getVolatility(4, 2), TOLERANCE);
    assertEquals(8.4, shiftedSurface.getVolatility(3, 3), TOLERANCE);
    assertEquals(9.6, shiftedSurface.getVolatility(4, 3), TOLERANCE);
  }
}
