/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DermanKaniImpliedBinomialTreeModelTest {
  private static final double SPOT = 100;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final double B = 0.05;
  private static final double ATM_VOL = 0.15;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(SPOT, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 5)), true);
  private static final ImpliedTreeModel<OptionDefinition, StandardOptionDataBundle> MODEL = new DermanKaniImpliedBinomialTreeModel(5);
  private static final Function<Double, Double> SMILE = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... tk) {
      Validate.isTrue(tk.length == 2);
      final double k = tk[1];
      return ATM_VOL + (SPOT - k) * 0.0005;
    }

  };
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(R, B, new VolatilitySurface(FunctionalDoublesSurface.from(SMILE)), SPOT, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getImpliedTrees(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getImpliedTrees(OPTION, null);
  }

  @Test
  public void test() {
    final Double[][] expectedSpot = new Double[][] {new Double[] {100. }, new Double[] {86.07, 116.18 }, new Double[] {70.49, 100., 131.94 }, new Double[] {60.63, 85.97, 116.32, 148.04 },
        new Double[] {44.05, 70.46, 100., 132.13, 163.24 }, new Double[] {41.04, 60.47, 85.86, 116.47, 148.14, 177.53 } };
    final Double[][] expectedLocalVol = new Double[][] {new Double[] {.145 }, new Double[] {.163, .128 }, new Double[] {.174, .146, .110 }, new Double[] {.205, .164, .128, .091 },
        new Double[] {.172, .175, .147, .109, .073 } };
    final ImpliedTreeResult result = MODEL.getImpliedTrees(OPTION, DATA);
    final Double[][] spot = result.getSpotPriceTree().getNodes();
    assertEquals(spot.length, expectedSpot.length);
    for (int i = 0; i < expectedSpot.length; i++) {
      for (int j = 0; j < expectedSpot[i].length; j++) {
        assertEquals(spot[i][j], expectedSpot[i][j], 1e-2);
      }
    }
    final Double[][] localVol = result.getLocalVolatilityTree().getNodes();
    assertEquals(localVol.length, expectedLocalVol.length);
    for (int i = 0; i < expectedLocalVol.length; i++) {
      for (int j = 0; j < expectedLocalVol[i].length; j++) {
        assertEquals(localVol[i][j], expectedLocalVol[i][j], 1e-3);
      }
    }
  }
}
