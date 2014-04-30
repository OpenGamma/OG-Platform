/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing;

import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.GreekVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FiniteDifferenceGreekVisitorTest {
  private static final Function1D<StandardOptionDataBundle, Double> FUNCTION = new Function1D<StandardOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardOptionDataBundle x) {
      return 0.;
    }

  };
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(1.)), 0.03, new VolatilitySurface(ConstantDoublesSurface.from(0.1)), 100.,
      DateUtils.getUTCDate(2010, 5, 1));
  private static final OptionDefinition DEFINITION = new EuropeanVanillaOptionDefinition(110, new Expiry(DateUtils.getUTCDate(2011, 5, 1)), true);
  private static final GreekVisitor<Double> VISITOR = new FiniteDifferenceGreekVisitor<>(FUNCTION, DATA, DEFINITION);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new FiniteDifferenceGreekVisitor<>(null, DATA, DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new FiniteDifferenceGreekVisitor<>(FUNCTION, null, DEFINITION);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    new FiniteDifferenceGreekVisitor<>(FUNCTION, DATA, null);
  }

  @Test
  public void testNotCalculated() {
    assertNull(VISITOR.visitDZetaDVol());
    assertNull(VISITOR.visitDriftlessTheta());
    assertNull(VISITOR.visitStrikeDelta());
    assertNull(VISITOR.visitStrikeGamma());
    assertNull(VISITOR.visitZeta());
    assertNull(VISITOR.visitZetaBleed());
  }
}
