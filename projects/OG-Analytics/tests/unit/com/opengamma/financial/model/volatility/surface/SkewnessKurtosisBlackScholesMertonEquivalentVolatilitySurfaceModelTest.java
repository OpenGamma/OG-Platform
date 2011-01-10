/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModelTest {
  private static final VolatilitySurfaceModel<OptionDefinition, SkewKurtosisOptionDataBundle> MODEL = new SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModel();
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1)), true);
  private static final double SIGMA = 0.4;
  private static final SkewKurtosisOptionDataBundle DATA = new SkewKurtosisOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.02)), 0.02, new VolatilitySurface(
      ConstantDoublesSurface.from(SIGMA)), 100, DATE, 0,
      3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullOption() {
    MODEL.getSurface(null, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getSurface(OPTION, null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getSurface(OPTION, DATA).getVolatility(DoublesPair.of(1., 1.)), SIGMA, 1e-15);
  }
}
