/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
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
public class TwoAssetAnalyticOptionModelTest {
  private static final double RESULT = 100;
  private static final Function1D<StandardTwoAssetOptionDataBundle, Double> F = new Function1D<StandardTwoAssetOptionDataBundle, Double>() {

    @Override
    public Double evaluate(final StandardTwoAssetOptionDataBundle x) {
      return RESULT;
    }

  };
  private static final TwoAssetAnalyticOptionModel<OptionDefinition, StandardTwoAssetOptionDataBundle> DUMMY = new TwoAssetAnalyticOptionModel<OptionDefinition, StandardTwoAssetOptionDataBundle>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Function1D<StandardTwoAssetOptionDataBundle, Double> getPricingFunction(final OptionDefinition definition) {
      return F;
    }
  };
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final OptionDefinition OPTION = new EuropeanVanillaOptionDefinition(100, new Expiry(DATE), true);
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.1)), 0, 0, new VolatilitySurface(
      ConstantDoublesSurface.from(0.1)), new VolatilitySurface(ConstantDoublesSurface.from(0.15)), 100, 90, 1, DATE);
  private static final Set<Greek> REQUIRED_GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA, Greek.GAMMA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    DUMMY.getGreeks(null, DATA, REQUIRED_GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    DUMMY.getGreeks(OPTION, null, REQUIRED_GREEKS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullGreeks() {
    DUMMY.getGreeks(OPTION, DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyGreeks() {
    DUMMY.getGreeks(OPTION, DATA, Collections.<Greek> emptySet());
  }

  @Test
  public void test() {
    final GreekResultCollection result = DUMMY.getGreeks(OPTION, DATA, REQUIRED_GREEKS);
    assertEquals(result.size(), 1);
    assertEquals(result.get(Greek.FAIR_PRICE), RESULT, 0);
  }
}
