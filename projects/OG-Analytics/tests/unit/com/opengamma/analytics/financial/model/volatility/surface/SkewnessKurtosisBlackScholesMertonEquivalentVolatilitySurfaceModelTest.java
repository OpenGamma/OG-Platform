/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModel;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModelTest {
  private static final VolatilitySurfaceModel<OptionDefinition, SkewKurtosisOptionDataBundle> MODEL = new SkewnessKurtosisBlackScholesMertonEquivalentVolatilitySurfaceModel();
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1)), true);
  private static final double SIGMA = 0.4;
  private static final SkewKurtosisOptionDataBundle DATA = new SkewKurtosisOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.02)), 0.02, new VolatilitySurface(
      ConstantDoublesSurface.from(SIGMA)), 100, DATE, 0,
      3);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOption() {
    MODEL.getSurface(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getSurface(OPTION, null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getSurface(OPTION, DATA).getVolatility(DoublesPair.of(1., 1.)), SIGMA, 1e-15);
  }
}
